/*
This file is part of sensus-persistence (SessionFactory on Hibernate).

Sensus-persistence is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Sensus-persistence is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with sensus-persistence.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.com.ziben.persistence;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Class to handle DAOs by a generic hibernate session factoring;
 * essa classe deve ser usada qunado se quer mais controle do
 * session factory, como acessos a session e transaction para queries
 * 
 * @author ccardozo
 *
 */
public abstract class SimpleSessionDAO {
	
	private Logger log = Logger.getLogger("SimpleSessionDAO");
	
    public Session session;
    public Transaction tx;

	public SimpleSessionDAO() {
        HibernateFactory.buildIfNeeded();
    }
    
    public void handleException(HibernateException e) throws DataAccessLayerException {
        HibernateFactory.rollback(tx);
        throw new DataAccessLayerException(e);
    }

    public void close() {
        HibernateFactory.close(session);
    }
    
    public void startOperation() throws HibernateException {
        log.debug(">>SimpleSessionDAO:startOperation()");
        session = HibernateFactory.openSession();
        tx = session.beginTransaction();
        log.debug("<<SimpleSessionDAO:startOperation()");
    }
}
