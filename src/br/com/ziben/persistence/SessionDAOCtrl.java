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



/**
 * Class to handle DAOs by a generic hibernate session factoring
 * @author ccardozo
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class SessionDAOCtrl extends GenericSessionDAO<SessionDAOCtrl> {
	
	private static final Logger log = Logger.getLogger(SessionDAOCtrl.class);
	
//    private Session sessionCtrl;
//    private Transaction txCtrl;

	/**
	 * Get the class that extends me, well...
	 */
	public SessionDAOCtrl() {
		super.openSession();
    }
	
    /**
     * just return the current session
     * @return
     */
	public Session getSession() {
		return super.getSession();
	}
	
	
    public void beginTransaction() throws HibernateException {
        super.beginTransaction();
    }
    
    public void commitTransaction() throws HibernateException {
        super.commitTransaction();
    }
    
    public void rollbackTransaction() throws HibernateException {
        super.rollbackTransaction();
    }

}

