package jss.database;

/**
 * Klasa, która umożliwia zapamiętanie, czy obiekt jest nowy czy pobrany z bazy
 * danych.
 * 
 * @author lukas
 */
public abstract class DbEntity {

	/**
	 * Czy przy zapisie obiektu zostanie wykonane zapytanie UPDATE?
	 */
	private boolean _isQueryUpdate = false;

	public DbEntity() {

	}

	/**
	 * Czy przy zapisie obiektu zostanie wykonane zapytanie UPDATE? Jeżli nie,
	 * wykonane zostanie INSERT.
	 * 
	 * @return czy przy obiektu zostanie wykonane zapytanie UPDATE?
	 */
	boolean isQueryUpdate() {
		return this._isQueryUpdate;
	}

	/**
	 * Ustawia, czy przy zapisie obiektu ma zostać wykonane zapytanie UPDATE? Jeżeli
	 * nie, wykonane zostanie INSERT.
	 * 
	 * @param selected czy przy zapisie ma zostać wykonane zapytanie UPDATE?
	 */
	void setQueryUpdate(boolean update) {
		this._isQueryUpdate = update;
	}

}