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
