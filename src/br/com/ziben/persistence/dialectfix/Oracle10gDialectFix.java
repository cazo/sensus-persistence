package br.com.ziben.persistence.dialectfix;

import java.sql.Types;

import org.hibernate.dialect.Oracle10gDialect;

/**
 * Correção para um bug conhecido mas rejeitado pela equipe do Hibernate<br>
 * Links de 2 reports que achei no jira do hibernate:<br>
 * <a href=https://hibernate.onjira.com/browse/HHH-5569>https://hibernate.onjira.com/browse/HHH-5569</a><br>
 * <a href=https://hibernate.onjira.com/browse/HHH-6726>https://hibernate.onjira.com/browse/HHH-6726</a>
 * 
 * @author fcjabulka
 * 
 */
public class Oracle10gDialectFix extends Oracle10gDialect {
	public Oracle10gDialectFix() {
		super();
		registerColumnType(Types.LONGVARCHAR, "clob");
		registerColumnType(Types.LONGNVARCHAR, "clob");
	}
}
