package at.ac.tuwien.complang.sbc11.workers;

public class Worker {
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}

/*
	Was machen die einzelnen Worker?
	
	PRODUCER:
		Erstellt Parts und stellt sie im gemeinsamen Arbeitsbereich zur Verf�gung.
		Parts k�nnen defekt sein oder nicht.
	ASSEMBLER:
		Entfernt n�tige Parts aus dem gemeinsamen Arbeitsbereich und setzt sie zu einem
		Computer zusammen. Stellt den neuen Computer dann im gemeinsamen
		Arbeitsbereich zur Verf�gung.
	TESTER:
		Liest ungetestete Computer aus dem gemeinsamen Arbeitsbereich und testet diese.
		Ist der Test erfolgreich, wird der Computer im Arbeitsbereich als fehlerfrei markiert.
		Schl�gt der Test fehl, wird der Computer im Arbeitsbereich als fehlerhaft markiert.
	LOGISTICIAN:
		Entfernt getestete Computer aus dem gemeinsamen Arbeitsbereich.
		Ist der Computer fehlerhaft, wird er in den Bereich f�r defekte Computer verschoben.
		Ist der Computer in Ordnung, wird er als fertig markiert (und in einen Bereich f�r
		fertige Computer verschoben???).
*/