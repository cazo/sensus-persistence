package br.com.ziben.persistence;

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * Classe para coisas de Hibernate Factoring
 * @author ccardozo
 *
 */
public class HibernateFactory {
    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
    private static Logger log = Logger.getLogger(HibernateFactory.class);

    /**
     * Construa um novo Singleton SessionFactory
     * @return
     * @throws HibernateException
     */
    public static SessionFactory buildSessionFactory() throws HibernateException {
        if (sessionFactory != null) {
            closeFactory();
        }
        return configureSessionFactory();
    }

    /**
     * Construa um SessionFactory, se já não tiver sido criado.
     */
    public static SessionFactory buildIfNeeded() throws DataAccessLayerException{
        if (sessionFactory != null) {
            return sessionFactory;
        }
        try {
            return configureSessionFactory();
        } catch (HibernateException e) {
            throw new DataAccessLayerException(e);
        }
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    

    public static Session openSession() throws HibernateException {
        buildIfNeeded();
        return sessionFactory.openSession();
    }

    public static void closeFactory() {
        if (sessionFactory != null) {
            try {
                sessionFactory.close();
            } catch (HibernateException ignored) {
                log.error("Não foi possível fechar a SessionFactory", ignored);
            }
        }
    }

    public static void close(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (HibernateException ignored) {
                log.error("Não foi possivel fechar a sessão", ignored);
            }
        }
    }

    public static void rollback(Transaction tx) {
        try {
            if (tx != null) {
                tx.rollback();
            }
        } catch (HibernateException ignored) {
            log.error("Não foi possível fazer rollback Transaction", ignored);
        }
    }
    
    /**
     *
     * @return
     * @throws HibernateException
     */
    private static SessionFactory configureSessionFactory() throws HibernateException {
		log.info(">> configureSessionFactory()");

		try {
			String nomeArquivo = System.getProperty("persistence.configuration");
			if (nomeArquivo == null) {
				nomeArquivo = "./" + "hibernate.cfg.xml";
				log.info(">> configureSessionFactory(): Setando arquivo de configuracoes " + nomeArquivo);
			}
			log.debug(">> configureSessionFactory() arquivo de configuracao: " + nomeArquivo);
			File configFile = new File(nomeArquivo);

			Configuration configuration = new Configuration();
			configuration.configure(configFile);
			serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Exception e) {
			e.printStackTrace();
		}

        return sessionFactory;
    }
}
