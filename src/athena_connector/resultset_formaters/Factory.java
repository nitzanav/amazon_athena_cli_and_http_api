package athena_connector.resultset_formaters;

public class Factory {
	public static IResultSetFormatter create(String className) throws ClassNotFoundException {
		switch (className) {
		case "SingleValue":
			return new SingleValue();
		default:
			throw new ClassNotFoundException(className);
		}
	}
}
