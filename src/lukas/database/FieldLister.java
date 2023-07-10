package lukas.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lukas.database.annotations.DbField;
import lukas.database.annotations.DbForeignKey;
import lukas.database.annotations.DbManyToOne;
import lukas.database.annotations.DbOneToOne;
import lukas.database.annotations.DbPrimaryKey;
import lukas.database.annotations.DbTable;
import lukas.database.annotations.DbView;
import lukas.database.annotations.DbViewField;
import lukas.database.annotations.DbViewObject;
import lukas.database.mappers.SqlTypeMapper;
import lukas.database.types.SqlType;

public class FieldLister {
	private final Database db;
	private final String tableName;// table name
	private final Class<?> tableClass;// entity class
	private final DbView dbView;

	private final List<DbFieldInfo> dbFields = new ArrayList<>();
	private final List<DbManyToOneInfo> dbManyToOne = new ArrayList<>();
	private final List<DbOneToOneInfo> dbOneToOne = new ArrayList<>();

	private final List<DbViewFieldInfo> dbViewFields = new ArrayList<>();
	private final List<DbViewObjectInfo> dbViewObjects = new ArrayList<>();

	private boolean lazyLoadedFields = false;// loaded fields by listFields()?
	private boolean lazyLoadedDatabaseTypes = false;// loaded database types?

	/**
	 * @param db         database framework instance
	 * @param tableClass entity class
	 * @throws DatabaseException no DbTable or DbView annotation
	 */
	FieldLister(Database db, Class<?> tableClass) throws DatabaseException {
		this.db = db;

		// get table name and class
		if (tableClass.isAnnotationPresent(DbView.class)) {// view
			dbView = tableClass.getAnnotation(DbView.class);
			this.tableName = dbView.name();
		} else if (tableClass.isAnnotationPresent(DbTable.class)) {// table
			this.tableName = tableClass.getAnnotation(DbTable.class).value();
			dbView = null;
		} else {
			throw new DatabaseException("No 'DbTable' or 'DbView' annotation in class " + tableClass.getName());
		}

		this.tableClass = tableClass;
	}

	/**
	 * List fields from table class (and superclasses)
	 * 
	 * @throws DatabaseException
	 */
	void listFields() throws DatabaseException {
		if (lazyLoadedFields) {// if fields loaded, do nothing
			return;
		}

		// listuj pola klasy oraz klas nadrzędnych
		Class<?> clazz = tableClass;

		if (isView()) {
			do {
				listFieldsForView(clazz);
				clazz = clazz.getSuperclass();
			} while (!clazz.equals(Object.class));
		} else {// table
			do {
				listFields(clazz);
				clazz = clazz.getSuperclass();
			} while (!clazz.equals(Object.class));
		}

		lazyLoadedFields = true;// loaded fields
	}

	/**
	 * List fields from class - for table
	 * 
	 * @param clazz class to list fields
	 * @throws DatabaseException
	 */
	private void listFields(Class<?> clazz) throws DatabaseException {
		Field[] fields = clazz.getDeclaredFields();// pobierz wszystkie pola z klasy

		for (Field field : fields) {
			field.setAccessible(true); // możliwość dostępu nawet jeżeli pole jest prywatne

			if (field.isAnnotationPresent(DbField.class)) {// database field
				DbFieldInfo info = processDbField(field, clazz);
				dbFields.add(info);

				if (field.isAnnotationPresent(DbPrimaryKey.class)) {// primary key field
					DbPrimaryKeyInfo pkInfo = processDbPrimaryKey(field);
					info.primaryKeyInfo = pkInfo;
				}

				if (field.isAnnotationPresent(DbForeignKey.class)) {// foreign key field
					DbForeignKeyInfo fkInfo = processDbForeignKey(field, clazz);
					info.foreignKeyInfo = fkInfo;
				}
			} else if (field.isAnnotationPresent(DbManyToOne.class)) {// many to one collection
				DbManyToOneInfo info = processDbManyToOne(field);
				dbManyToOne.add(info);
			} else if (field.isAnnotationPresent(DbOneToOne.class)) {// one to one relation
				DbOneToOneInfo info = processDbOneToOne(field);
				dbOneToOne.add(info);
			}

		} // for fields
	}

	/**
	 * List fields from class - for view
	 * 
	 * @param clazz class to list fields
	 * @throws DatabaseException
	 */
	private void listFieldsForView(Class<?> clazz) throws DatabaseException {
		Field[] fields = clazz.getDeclaredFields();// pobierz wszystkie pola z klasy

		for (Field field : fields) {
			field.setAccessible(true); // możliwość dostępu nawet jeżeli pole jest prywatne

			if (field.isAnnotationPresent(DbViewField.class)) {// view field
				DbViewFieldInfo info = processDbViewField(field, clazz);
				dbViewFields.add(info);
			} else if (field.isAnnotationPresent(DbViewObject.class)) {// view object
				dbViewObjects.add(processDbViewObject(field, clazz));
			}
		}
	}

	/**
	 * Load database types
	 * 
	 * @param mapper native type to SQL type mapper
	 * @throws DatabaseException cannot determine type, or error when listing fields
	 */
	void loadDatabaseTypes(SqlTypeMapper mapper) throws DatabaseException {
		if (lazyLoadedDatabaseTypes) {// if loaded types, do nothing
			return;
		}

		if (!lazyLoadedFields) {// if fields not loaded - load it
			this.listFields();
		}

		for (DbFieldInfo fieldInfo : dbFields) {
			Class<?> nativeType = fieldInfo.getNativeType();// native java type

			// sql type
			SqlType sqlType = fieldInfo.fieldAnnotation.type();
			if (sqlType == SqlType.DEFAULT) {// default type - determine
				sqlType = mapper.getSqlType(nativeType);
			}

			if (sqlType == SqlType.UNKNOWN) {// cannot determine type
				throw new DatabaseException("Cannot determine SQL type of " + nativeType.getName());
			}

			fieldInfo.sqlType = sqlType;// SQL type

			// get sql string type for current database
			String fieldSqlStr = db.getDb().getTypeString(fieldInfo);
			if (fieldSqlStr == null) {
				throw new DatabaseException("Cannot get type " + fieldInfo.sqlType + " for database!");
			}

			DbField dbField = fieldInfo.getFieldAnnotation();

			// replace string len value
			int strLen = dbField.stringLen();
			if (strLen == -1) {
				strLen = db.getConfig().defaultStringLen;
			}
			fieldSqlStr = fieldSqlStr.replace("{strlen}", Integer.toString(strLen));

			// replace decimal precision
			int precistion = dbField.decimalPrecision();
			if (precistion == -1) {
				precistion = db.getConfig().defaultDecimalPrecision;
			}
			fieldSqlStr = fieldSqlStr.replace("{precision}", Integer.toString(precistion));

			// replace decimal scale
			int scale = dbField.decimalScale();
			if (scale == -1) {
				scale = db.getConfig().defaultDecimalScale;
			}
			fieldSqlStr = fieldSqlStr.replace("{scale}", Integer.toString(scale));

			fieldInfo.sqlTypeString = fieldSqlStr;// SQL type string
		}
	}

	/**
	 * Process DbField annotation
	 * 
	 * @param field field in class
	 * @param clazz current processing class
	 * @return database field info
	 */
	private DbFieldInfo processDbField(Field field, Class<?> clazz) {
		DbField dbField = field.getAnnotation(DbField.class);// pobierz element adnotacji

		DbFieldInfo info = new DbFieldInfo();
		info.fieldAnnotation = dbField;
		info.clazz = clazz;
		info.fieldInClass = field;

		// in table name
		info.inTableName = dbField.name();// custom name
		if (info.inTableName.isEmpty()) {// if custom name empty, use class name
			info.inTableName = field.getName();
		}

		return info;
	}

	/**
	 * Process DbPrimaryKey annotation
	 * 
	 * @param field field info
	 * @return primary key info
	 * @throws DatabaseException
	 */
	private DbPrimaryKeyInfo processDbPrimaryKey(Field field) throws DatabaseException {
		if (!field.isAnnotationPresent(DbField.class)) {
			throw new DatabaseException("No annotation DbField on DbPrimaryKey!");
		}

		DbPrimaryKey dbPrimaryKey = field.getAnnotation(DbPrimaryKey.class);

		DbPrimaryKeyInfo info = new DbPrimaryKeyInfo();
		info.pkAnnotation = dbPrimaryKey;

		return info;
	}

	/**
	 * Process DbForeignKey annotation
	 * 
	 * @param field field in class
	 * @param clazz current processing class
	 * @return foreign key info
	 * @throws DatabaseException
	 */
	private DbForeignKeyInfo processDbForeignKey(Field field, Class<?> clazz) throws DatabaseException {
		if (!field.isAnnotationPresent(DbField.class)) {
			throw new DatabaseException("No annotation DbField on DbForeignKey!");
		}

		DbForeignKey dbForeignKey = field.getAnnotation(DbForeignKey.class);

		DbForeignKeyInfo info = new DbForeignKeyInfo();
		info.fkAnnotation = dbForeignKey;

		// load this field - field in current class for place foreing object
		if (!dbForeignKey.thisField().trim().equals("")) {
			try {
				info.thisField = getDeclaredField(clazz, dbForeignKey.thisField());
			} catch (NoSuchFieldException e) {
				String fname = field.getName();// field name
				String cname = clazz.getName();// class name
				throw new DatabaseException("Cannot set foreign field " + fname + " for class: " + cname, e);
			}
		} else {
			info.thisField = null;// if not field for save foreign object, set null
		}

		// reference class
		Class<?> refObj = dbForeignKey.refObject();

		// check annotation DbTable
		if (!refObj.isAnnotationPresent(DbTable.class)) {
			throw new DatabaseException("No 'DbTable' annotation in foreign class: " + refObj.getName());
		}

		// get table name
		DbTable foreignDbTable = refObj.getAnnotation(DbTable.class);
		info.refTableName = foreignDbTable.value();

		String refFieldName = dbForeignKey.refField();// field name
		String refClassName = dbForeignKey.refObject().getName();// class name

		// load foreign field
		Field foreignField;
		try {
			foreignField = getDeclaredField(dbForeignKey.refObject(), dbForeignKey.refField());
		} catch (NoSuchFieldException e) {
			throw new DatabaseException("Cannot found foreign field " + refFieldName + " in class: " + refClassName, e);
		}

		// check foreign field annotation
		if (!foreignField.isAnnotationPresent(DbField.class)) {
			throw new DatabaseException("No DbField annotation: " + refClassName + "::" + refFieldName);
		}
		DbField dbForeignField = foreignField.getAnnotation(DbField.class);

		// check is unique or primary key
		// TODO

		// save table column name
		info.refInTableName = dbForeignField.name();// custom column name
		if (info.refInTableName.isEmpty()) {// default column name
			info.refInTableName = foreignField.getName();
		}

		return info;
	}

	/**
	 * Process DbManyToOne annotation
	 * 
	 * @param field field info
	 * @throws DatabaseException
	 */
	private DbManyToOneInfo processDbManyToOne(Field field) throws DatabaseException {
		DbManyToOne dbMany = field.getAnnotation(DbManyToOne.class);

		// sprawdź, czy pole ManyToOne jest kolekcją
		if (!Collection.class.isAssignableFrom(field.getType())) {// jeżeli nie jest - rzuć wyjątek
			throw new DatabaseException("Field '" + field.getName() + "' in class '"
					+ field.getDeclaringClass().getName() + "' ManyToOne relation is not a collection!");
		}

		DbManyToOneInfo info = new DbManyToOneInfo();
		info.mtoAnnotation = dbMany;
		info.fieldInClass = field;

		// pobierz pole w klasie obcej
		try {
			Field refField = getDeclaredField(dbMany.refObject(), dbMany.refField());

			if (!refField.isAnnotationPresent(DbField.class) || !refField.isAnnotationPresent(DbForeignKey.class)) {
				throw new DatabaseException("Relation field " + refField.getName() + " has not FK annotation!");
			}

			DbForeignKey dbForKey = refField.getAnnotation(DbForeignKey.class);

			// ustaw pole, do którego odwołuje się ManyToOne
			if (!dbForKey.thisField().isEmpty()) {// jeżeli takowe istnieje
				Field refFieldOnClass = getDeclaredField(dbMany.refObject(), dbForKey.thisField());
				info.refFieldObj = refFieldOnClass;
			}

			info.refFieldKey = refField;
		} catch (NoSuchFieldException e) {
			throw new DatabaseException("DbManyToOne field not found!", e);
		}

		return info;
	}

	/**
	 * Process DbOneToOne annotation
	 * 
	 * @param field field info
	 * @throws DatabaseException
	 */
	private DbOneToOneInfo processDbOneToOne(Field field) throws DatabaseException {
		DbOneToOne dbOne = field.getAnnotation(DbOneToOne.class);

		DbOneToOneInfo info = new DbOneToOneInfo();
		info.otoAnnotation = dbOne;
		info.fieldInClass = field;

		// pobierz pole w klasie obcej
		try {
			Field refField = getDeclaredField(dbOne.refObject(), dbOne.refField());

			if (!refField.isAnnotationPresent(DbField.class) || !refField.isAnnotationPresent(DbForeignKey.class)) {
				throw new DatabaseException("Relation field " + refField.getName() + " has not FK annotation!");
			}

			DbForeignKey dbForKey = refField.getAnnotation(DbForeignKey.class);

			// ustaw pole, do którego odwołuje się ManyToOne
			if (!dbForKey.thisField().isEmpty()) {// jeżeli takowe istnieje
				Field refFieldOnClass = getDeclaredField(dbOne.refObject(), dbForKey.thisField());
				info.refFieldObj = refFieldOnClass;
			}

			info.refFieldKey = refField;
		} catch (NoSuchFieldException e) {
			throw new DatabaseException("DbOneToOne field not found!", e);
		}

		return info;
	}

	/**
	 * Process DbViewFiled annotation
	 * 
	 * @param field field in class
	 * @param clazz current processing class
	 * @return DbViewField info
	 */
	private DbViewFieldInfo processDbViewField(Field field, Class<?> clazz) {
		DbViewField dbViewField = field.getAnnotation(DbViewField.class);// pobierz element adnotacji

		DbViewFieldInfo info = new DbViewFieldInfo();
		info.fieldAnnotation = dbViewField;
		// info.clazz = clazz;
		info.fieldInClass = field;

		// in table name
		info.inQueryName = dbViewField.value();// custom name
		if (info.inQueryName.isEmpty()) {// if custom name empty, use class name
			info.inQueryName = field.getName();
		}

		return info;
	}

	/**
	 * Process DbViewObject annotation
	 * 
	 * @param field field in class
	 * @param clazz current processing class
	 * @return DbViewObject info
	 */
	private DbViewObjectInfo processDbViewObject(Field field, Class<?> clazz) {
		DbViewObject dbViewObject = field.getAnnotation(DbViewObject.class);// pobierz element adnotacji

		DbViewObjectInfo info = new DbViewObjectInfo();
		info.fieldAnnotation = dbViewObject;
		// info.clazz = clazz;
		info.fieldInClass = field;

		return info;
	}

	/**
	 * Get declared field from class or superclass
	 * 
	 * @param clazz class to start search
	 * @param field field name
	 * @return field in class
	 * @throws NoSuchFieldException field not found
	 */
	private Field getDeclaredField(Class<?> clazz, String field) throws NoSuchFieldException {
		do {
			try {
				Field f = clazz.getDeclaredField(field);
				f.setAccessible(true);
				return f;
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			}
		} while (!clazz.equals(Object.class));

		throw new NoSuchFieldException("Field not found: " + field);
	}

	/**
	 * @return table name
	 */
	String getTableName() {
		return tableName;
	}

	/**
	 * @return table class
	 */
	Class<?> getTableClass() {
		return tableClass;
	}

	/**
	 * @return database fields in class
	 */
	List<DbFieldInfo> getDbFields() {
		return new ArrayList<>(dbFields);// copy
	}

	/**
	 * @return database fields or view fields in class
	 */
	List<DbFieldOrViewInfo> getDbFieldOrViewInfo() {
		List<DbFieldOrViewInfo> ret = new ArrayList<>(dbFields);// copy
		ret.addAll(dbViewFields);
		return ret;
	}

	/**
	 * @return database fields which are primary keys
	 */
	List<DbFieldInfo> getPrimaryKeys() {
		List<DbFieldInfo> pkList = new ArrayList<>(2);

		for (DbFieldInfo dfi : dbFields) {
			if (dfi.getPrimaryKeyInfo() != null) {
				pkList.add(dfi);
			}
		}

		return pkList;
	}

	/**
	 * @return database fields which are foreign keys
	 */
	List<DbFieldInfo> getForeignKeys() {
		List<DbFieldInfo> fkList = new ArrayList<>(2);

		for (DbFieldInfo dfi : dbFields) {
			if (dfi.getForeignKeyInfo() != null) {
				fkList.add(dfi);
			}
		}

		return fkList;
	}

	/**
	 * @return database fields which are indexes
	 */
	List<DbFieldInfo> getIndexes() {
		List<DbFieldInfo> indexes = new ArrayList<>(2);

		for (DbFieldInfo dfi : dbFields) {
			if (dfi.fieldAnnotation.isIndex()) {
				indexes.add(dfi);
			}
		}

		return indexes;
	}

	/**
	 * Get field by name in class
	 * 
	 * @param string field name in class
	 * @return field, or null if not found
	 */
	DbFieldOrViewInfo getFieldByInClassName(String name) {
		for (DbFieldInfo df : dbFields) {
			if (df.getFieldInClass().getName().equals(name)) {
				return df;
			}
		}
		for (DbViewFieldInfo df : dbViewFields) {
			if (df.getFieldInClass().getName().equals(name)) {
				return df;
			}
		}
		return null;
	}

	/**
	 * @return fields for ManyToOne relations
	 */
	List<DbManyToOneInfo> getDbManyToOne() {
		return dbManyToOne;
	}

	/**
	 * @return fields for OneToOne relations
	 */
	List<DbOneToOneInfo> getDbOneToOne() {
		return dbOneToOne;
	}

	/**
	 * @return this is view?
	 */
	boolean isView() {
		return dbView != null;
	}

	/**
	 * @return DbView annotation
	 */
	DbView getDbView() {
		return dbView;
	}

	/**
	 * @return DbViewObject list info for view
	 */
	List<DbViewObjectInfo> getDbViewObjects() {
		return dbViewObjects;
	}

	/**
	 * Database field info
	 * 
	 * @author lukas
	 */
	public class DbFieldInfo implements DbFieldOrViewInfo {
		DbFieldInfo() {// not public constructor
		}

		/**
		 * DbField annotation
		 */
		DbField fieldAnnotation;

		/**
		 * Class in which the field is located (for superclasses support)
		 */
		Class<?> clazz;

		/**
		 * Field in class
		 */
		Field fieldInClass;

		/**
		 * Column name in table
		 */
		String inTableName;

		/**
		 * SQL type
		 */
		SqlType sqlType;

		/**
		 * SQL type string for current database type
		 */
		String sqlTypeString;

		/**
		 * Primary key info
		 */
		DbPrimaryKeyInfo primaryKeyInfo;

		/**
		 * Foreign key info
		 */
		DbForeignKeyInfo foreignKeyInfo;

		/**
		 * @return DbField annotation
		 */
		public DbField getFieldAnnotation() {
			return fieldAnnotation;
		}

		@Override
		public Field getFieldInClass() {
			return fieldInClass;
		}

		/**
		 * @return SQL type
		 */
		public SqlType getSqlType() {
			return sqlType;
		}

		/**
		 * @return primary key info (or null)
		 */
		public DbPrimaryKeyInfo getPrimaryKeyInfo() {
			return primaryKeyInfo;
		}

		/**
		 * @return foreign key info (or null)
		 */
		public DbForeignKeyInfo getForeignKeyInfo() {
			return foreignKeyInfo;
		}

		@Override
		public String getInQueryName() {
			return tableName + '_' + inTableName;
		}

		@Override
		public String getInTableName() {
			return inTableName;
		}
	}

	/**
	 * Primary key info
	 * 
	 * @author lukas
	 */
	public class DbPrimaryKeyInfo {
		DbPrimaryKeyInfo() {// not public constructor
		}

		/**
		 * DbPrimaryKey annotation
		 */
		DbPrimaryKey pkAnnotation;

		/**
		 * @return DbPrimaryKey annotation
		 */
		public DbPrimaryKey getPkAnnotation() {
			return pkAnnotation;
		}
	}

	/**
	 * Foreign key info
	 * 
	 * @author lukas
	 */
	public class DbForeignKeyInfo {
		DbForeignKeyInfo() {// not public constructor
		}

		/**
		 * DbForeignKey annotation
		 */
		DbForeignKey fkAnnotation;

		/**
		 * Field in class for store foreign object
		 */
		Field thisField;

		/**
		 * Foreign table name
		 */
		String refTableName;

		/**
		 * Foreign column name
		 */
		String refInTableName;

		/**
		 * @return DbForeignKey annotation
		 */
		public DbForeignKey getFkAnnotation() {
			return fkAnnotation;
		}
	}

	/**
	 * Many to one info
	 * 
	 * @author lukas
	 */
	public class DbManyToOneInfo {
		DbManyToOneInfo() {// not public constructor
		}

		/**
		 * DbManyToOne annotation
		 */
		DbManyToOne mtoAnnotation;

		/**
		 * Field in class
		 */
		Field fieldInClass;

		/**
		 * Field in ref class - foreign key field
		 */
		Field refFieldKey;

		/**
		 * Field in ref class - object field
		 */
		Field refFieldObj;

		/**
		 * @return DbManyToOne annotation
		 */
		public DbManyToOne getMtoAnnotation() {
			return mtoAnnotation;
		}
	}

	/**
	 * One to one info
	 * 
	 * @author lukas
	 */
	public class DbOneToOneInfo {
		DbOneToOneInfo() {// not public constructor
		}

		/**
		 * DbOneToOne annotation
		 */
		DbOneToOne otoAnnotation;

		/**
		 * Field in class
		 */
		Field fieldInClass;

		/**
		 * Field in ref class - foreign key field
		 */
		Field refFieldKey;

		/**
		 * Field in ref class - object field
		 */
		Field refFieldObj;

		/**
		 * @return DbManyToOne annotation
		 */
		public DbOneToOne getOtoAnnotation() {
			return otoAnnotation;
		}
	}

	/**
	 * VIEW field info
	 * 
	 * @author lukas
	 */
	public class DbViewFieldInfo implements DbFieldOrViewInfo {
		DbViewFieldInfo() {
		}

		/**
		 * DbViewField annotation
		 */
		DbViewField fieldAnnotation;

		/**
		 * Class in which the field is located (for superclasses support)
		 */
		// Class<?> clazz;

		/**
		 * Field in class
		 */
		Field fieldInClass;

		/**
		 * Column name in query
		 */
		String inQueryName;

		@Override
		public String getInQueryName() {
			return inQueryName;
		}

		@Override
		public String getInTableName() {
			return inQueryName;
		}

		@Override
		public Field getFieldInClass() {
			return fieldInClass;
		}
	}

	/**
	 * VIEW object info
	 * 
	 * @author lukas
	 */
	public class DbViewObjectInfo {
		DbViewObjectInfo() {
		}

		/**
		 * DbViewObject annotation
		 */
		DbViewObject fieldAnnotation;

		/**
		 * Class in which the field is located (for superclasses support)
		 */
		// Class<?> clazz;

		/**
		 * Field in class
		 */
		Field fieldInClass;
	}

	public interface DbFieldOrViewInfo {
		/**
		 * @return name in query
		 */
		public String getInQueryName();

		/**
		 * @return name in table
		 */
		public String getInTableName();

		/**
		 * @return field in class
		 */
		public Field getFieldInClass();

		/**
		 * @return native java type
		 */
		default public Class<?> getNativeType() {
			return getFieldInClass().getType();
		}

		/**
		 * @param clazz native type to check
		 * @return is native type?
		 */
		default public boolean nativeTypeIs(Class<?> clazz) {
			return getNativeType().isAssignableFrom(clazz);
		}
	}

}
