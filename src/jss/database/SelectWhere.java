package jss.database;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa dla warunku WHERE w zapytaniu SELECT.
 * @author lukas
 */
public class SelectWhere {
	private SelectWhere next;//następny warunek
	private SelectWhere inside;//warunek w środku
	private SelectWhere parent; //rodzic w warunku w środku
	
	String field;//nazwa pola w klasie encji
	Object value1;//wartość obiektu
	Object value2;//wartość obiektu drugiego - używane tylko w przypadku BETWEEN
	WhereConnector conn; //łącznik AND lub OR
	WhereType type; //typ sprawdzania
	
	/**
	 * Konstruktor domyślny, bezargumentowy
	 */
	public SelectWhere(){
		
	}
	
	/**
	 * Konstruktor dla tworzenia nowego obiektu posiadającego rodzica
	 * @param parent rodzic
	 */
	private SelectWhere(SelectWhere parent){
		this.parent = parent;
	}
	
	/**
	 * Konstruktor dla sprawdzania równości
	 * @param field nazwa pola
	 * @param value wartość
	 */
	public SelectWhere(String field, Object value){
		this(field, value, WhereType.EQUAL, WhereConnector.AND);
	}
	
	/**
	 * Konstruktor z możliwością ustawienia typu sprawdzania.
	 * @param field nazwa pola
	 * @param value wartość
	 * @param type typ sprawdzania
	 */
	public SelectWhere(String field, Object value, WhereType type){
		this(field, value, type, WhereConnector.AND);
	}
	
	/**
	 * Konstruktor z możliwością ustawienia typu sprawdzania i łącznika
	 * @param field nazwa pola
	 * @param value wartość 
	 * @param type typ sprawdzania
	 * @param conn łącznik (AND lub OR)
	 */
	public SelectWhere(String field, Object value, WhereType type, WhereConnector conn){
		this.field = field;
		this.value1 = value;
		this.conn = conn;
		this.type = type;
	}
	
	/**
	 * Ustawienie pola, wartości, typu sprawdzania i łącznika
	 * @param field nazwa pola
	 * @param value wartość
	 * @param type typ sprawdzania
	 * @param conn łącznik (AND lub OR)
	 * @return this object
	 */
	public SelectWhere set(String field, Object value, WhereType type, WhereConnector conn){
		this.field = field;
		this.value1 = value;
		this.type = type;
		this.conn = conn;
		return this;
	}
	
	/**
	 * Sprawdzanie, czy wartość pola jest pomiędzy dwiema wartościami
	 * @param field nazwa pola
	 * @param value1 pierwsza wartość
	 * @param value2 druga wartość
	 * @param conn łącznik (AND lub OR)
	 * @return this object
	 */
	public SelectWhere between(String field, Object value1, Object value2, WhereConnector conn){
		this.field = field;
		this.value1 = value1;
		this.value2 = value2;
		this.conn = conn;
		this.type = WhereType.BETWEEN;
		return this;
	}
	
	/**
	 * Sprawdzanie, czy wartość pola NIE jest pomiędzy dwiema wartościami
	 * @param field nazwa pola
	 * @param value1 pierwsza wartość
	 * @param value2 druga wartość 
	 * @param conn łącznik (AND lub OR)
	 * @return this object
	 */
	public SelectWhere notBetween(String field, Object value1, Object value2, WhereConnector conn){
		this.field = field;
		this.value1 = value1;
		this.value2 = value2;
		this.conn = conn;
		this.type = WhereType.NOT_BETWEEN;
		return this;
	}
	
	/**
	 * Sprawdzanie czy pole posiada wartość NULL
	 * @param field nazwa pola
	 * @param conn łącznik (AND lub OR)
	 * @return this object
	 */
	public SelectWhere isNull(String field, WhereConnector conn){
		this.field = field;
		this.conn = conn;
		this.type = WhereType.IS_NULL;
		return this;
	}
	
	/**
	 * Sprawdzanie czy pole NIE posiada wartości NULL
	 * @param field nazwa pola
	 * @param conn łącznik (AND lub OR)
	 * @return this object
	 */
	public SelectWhere isNotNull(String field, WhereConnector conn){
		this.field = field;
		this.conn = conn;
		this.type = WhereType.IS_NOT_NULL;
		return this;
	}
	
	/**
	 * Tworzenie następnego warunku (do iteracji)
	 * @return nowy obiekt warunku SelectWhere
	 */
	public SelectWhere createNext(){
		next = new SelectWhere(parent);
		return next;
	}
	
	/**
	 * Tworzenie warunku zagnieżdżony wewnątrz (do iteracji)
	 * @return nowy obiektu warunku SelectWhere znajdujący się wewnątrz obecnego - warunek zagnieżdżony
	 */
	public SelectWhere createInside(){
		inside = new SelectWhere(this);
		return inside;
	}
	
	/**
	 * Pobiera następny warunek
	 * @return następny warunek
	 */
	SelectWhere next(){
		return next;
	}
	
	/**
	 * Czy jest następny warunek?
	 * @return czy jest następny warunek?
	 */
	boolean hasNext(){
		return next != null;
	}
	
	/**
	 * Pobiera warunek zagnieżdżony wewnątrz istniejącego
	 * @return warunek zagnieżdżony
	 */
	SelectWhere inside(){
		return inside;
	}
	
	/**
	 * Czy jest warunek zagnieżdżony wewnątrz obecnego?
	 * @return czy jest warunek zagnieżdżony?
	 */
	boolean hasInside(){
		return inside != null;
	}
	
	/**
	 * Pobiera rodzica zagnieżdżonego warunku
	 * @return rodzic zagnieżdżonego warunku
	 */
	SelectWhere parent(){
		return parent;
	}
	
	/**
	 * Sprawdza czy warunek posiada rodzica - jest warunkiem zagnieżdżonym
	 * @return czy warunek posiada rodzica?
	 */
	boolean hasParent(){
		return parent != null;
	}
	
	/**
	 * Pobiera łącznik AND lub OR jako String
	 * @return j.w
	 */
	String connectorToString(){
		return (conn.equals(WhereConnector.AND))? "AND" : "OR";
	}
	
	/**
	 * Pobiera typ sprawdzania jako String
	 * @return j.w
	 */
	String typeToString(){
		return MAP_OPERATOR.get(type);
	}
	
	/**
	 * Czy warunek jest typu IS NULL lub IS NOT NULL
	 * @return j.w
	 */
	boolean isNullOrNotNull(){
		if(type.equals(WhereType.IS_NULL) || type.equals(WhereType.IS_NOT_NULL)) return true;
		else return false;
	}
	
	/**
	 * Czy warunek jest typu BETWEEN lub NOT BETWEEN
	 * @return j.w
	 */
	boolean isBetweenOrNotBetween(){
		if(type.equals(WhereType.BETWEEN) || type.equals(WhereType.NOT_BETWEEN)) return true;
		else return false;
	}
	
	/**
	 * Łącznik AND lub OR
	 * @author lukas
	 */
	public enum WhereConnector{
		
		/**
		 * Łącznik AND 
		 */
		AND,
		
		/**
		 * Łącznik OR 
		 */
		OR
	}
	
	/**
	 * Typ sprawdzania
	 * @author lukas
	 */
	public enum WhereType{
		/**
		 * Równy
		 */
		EQUAL,
		
		/**
		 * Różny od
		 */
		NOT_EQUAL,
		
		/**
		 * Większy
		 */
		GREATER,
		
		/**
		 * Większy lub równy
		 */
		GREATER_EQUAL,
		
		/**
		 * Mniejszy
		 */
		LESS,
		
		/**
		 * Mniejszy lub równy
		 */
		LESS_EQUAL,
		
		
		
		/**
		 * Jest pomiędzy dwiema wartościami
		 */
		BETWEEN,
		
		/**
		 * NIE jest pomiędzy dwiema wartościami
		 */
		NOT_BETWEEN,
		
		/**
		 * Jest NULL
		 */
		IS_NULL,
		
		/**
		 * NIE jest NULL
		 */
		IS_NOT_NULL,
	}
	
	/**
	 * Mapa typów sprawdzania na String
	 */
	private static final Map<WhereType,String> MAP_OPERATOR = new HashMap<WhereType,String>(){
		private static final long serialVersionUID = -2997627824444970513L;
		{
			put(WhereType.EQUAL, "=");
			put(WhereType.NOT_EQUAL, "<>");
			put(WhereType.GREATER, ">");
			put(WhereType.GREATER_EQUAL, ">=");
			put(WhereType.LESS, "<");
			put(WhereType.LESS_EQUAL, "<=");
			put(WhereType.BETWEEN, "BETWEEN");
			put(WhereType.NOT_BETWEEN, "NOT BETWEEN");
			put(WhereType.IS_NULL, "IS NULL");
			put(WhereType.IS_NOT_NULL, "IS NOT NULL");
		}
	};
}
