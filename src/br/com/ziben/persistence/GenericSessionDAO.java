package br.com.ziben.persistence;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

@SuppressWarnings("unchecked")
public abstract class GenericSessionDAO<T> {
    private Session session;
    private Transaction tx;
	private Class<T> classe;

	public GenericSessionDAO() {
		this.classe = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        HibernateFactory.buildIfNeeded();
    }

    protected void persist(T obj) {
        try {
            startOperation();
            session.saveOrUpdate(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
    }

    protected void delete(T obj) {
        try {
            startOperation();
            session.delete(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
    }

    protected Object find(String id) {
        Object obj = null;
        try {
            startOperation();
            obj = session.load(this.classe.getClass(), id);
            //tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
        return obj;
    }

    protected List<T> findAll(T clazz) {
        List<T> objects = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + this.classe.getName());
            objects = query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
        return objects;
    }

    protected void handleException(HibernateException e) throws DataAccessLayerException {
        HibernateFactory.rollback(tx);
        throw new DataAccessLayerException(e);
    }

    protected void startOperation() throws HibernateException {
        session = HibernateFactory.openSession();
        tx = session.beginTransaction();
    }
}
