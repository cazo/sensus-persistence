package br.com.ziben.persistence;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

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
        System.out.println("******* find() >>>>" + this.classe.toString());
        try {
            startOperation();
            obj = session.load(this.classe, id);
            session.flush();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            System.out.println("******* HibernateFactory.close(session) sem close() >>>>" + this.classe.toString());
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
        session.setFlushMode(FlushMode.MANUAL);
        ManagedSessionContext.bind(session);
        tx = session.beginTransaction();
    }
}
