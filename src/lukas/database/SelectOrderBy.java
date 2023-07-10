package lukas.database;

/**
 * Klasa do ustawiania sortowania (ORDER BY) w zapytaniu SELECT
 * @author lukas
 */
public class SelectOrderBy {
	private String field;
	private OrderBy order;
	
	/**
	 * Konstruktor dla sortowania po danym polu rosnąco
	 * @param field nazwa pola w klasie encji
	 */
	public SelectOrderBy(String field){
		this(field, OrderBy.ASC);
	}
	
	/**
	 * Konstruktor do sortowania po danym polu rosnąco lub malejąco
	 * @param field nazwa pola w klasie encji
	 * @param order rosnąco czy malejąco?
	 * @see OrderBy
	 */
	public SelectOrderBy(String field, OrderBy order){ 
		this.field = field;
		this.order = order;
	}
	
	/**
	 * Pobierz nazwe pola
	 * @return nazwa pola
	 */
	String getField(){
		return field;
	}
	
	/**
	 * Pobierz typ sortowania - rosnąco lub malejąco
	 * @return typ sortowania
	 */
	OrderBy getOrderBy(){
		return order;
	}
	
	/**
	 * Pobierz typ sortowania - rosnąco (ASC) lub malejąco (DESC) jako String
	 * @return typ sortowania jako string
	 */
	String getOrderByString(){
		return (order.equals(OrderBy.ASC))? "ASC" : "DESC";
	}
	
	/**
	 * Typ sortowania ORDER BY - rosnąco (ASC) lub malejąco (DESC)
	 * @author lukas
	 */
	public enum OrderBy{
		
		/**
		 * Sortowanie rosnąco 
		 */
		ASC,
		
		/**
		 * Sortowanie malejąco 
		 */
		DESC
	}
}
