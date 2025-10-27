package akses.ksei.co.id.investor.config;

import java.util.Locale;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class UppercasePhysicalNamingStrategy implements PhysicalNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return convertToUppercaseUnderscore(name);
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return convertToUppercaseUnderscore(name);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return convertToUppercaseUnderscore(name);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return convertToUppercaseUnderscore(name);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return convertToUppercaseUnderscore(name);
	}

	private Identifier convertToUppercaseUnderscore(Identifier name) {
		if (name == null) {
			return null;
		}
		// Convert camelCase to UPPERCASE with underscores
		String newName = name.getText().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
		return Identifier.toIdentifier(newName);
	}

}
