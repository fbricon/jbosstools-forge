package org.jboss.tools.aesh.core.console;

import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.tools.aesh.core.ansi.Document;
import org.jboss.tools.aesh.core.ansi.Style;
import org.jboss.tools.aesh.core.internal.ansi.Command;
import org.jboss.tools.aesh.core.internal.io.AeshInputStream;
import org.jboss.tools.aesh.core.internal.io.AeshOutputStream;

public abstract class AbstractConsole implements Console {
	
	private AeshInputStream inputStream = null;
	private AeshOutputStream outputStream, errorStream = null;
	private Document document = null;
	
	public AbstractConsole() {
		initialize();
	}

	public abstract void start();
	public abstract void stop();
	protected abstract void createConsole();

	public void connect(Document document) {
		if (this.document != null) {
			disconnect();
		}
		this.document = document;
	}
	
	public void disconnect() {
		document = null;
	}
	
	public void sendInput(String input) {
		if (inputStream != null) {
			inputStream.append(input);
		}
	}

	
	protected void initialize() {
		createStreams();
	}
	
	public void createStreams() {
		inputStream = createInputStream();
		outputStream = createOutputStream();
		errorStream = createErrorStream();
	}

	protected InputStream getInputStream() {
		return inputStream;
	}

	protected OutputStream getOutputStream() {
		return outputStream;
	}

	protected OutputStream getErrorStream() {
		return errorStream;
	}

	private AeshInputStream createInputStream() {
		return new AeshInputStream(); 
	}
	
	private AeshOutputStream createOutputStream() {
		return new AeshOutputStream() {			
			@Override
			public void onOutput(String string) {
				handleOutput(string);
			}			
			@Override
			public void onCommand(Command controlSequence) {
				handleControlSequence(controlSequence);
			}
		};
	}
	
	private AeshOutputStream createErrorStream() {
		return new AeshOutputStream() {			
			@Override
			public void onOutput(String string) {
				handleOutput(string);
			}			
			@Override
			public void onCommand(Command controlSequence) {
				handleControlSequence(controlSequence);
			}
		};
	}
	
	private void handleControlSequence(Command controlSequence) {
		if (document != null) {
			controlSequence.handle(document);
		}
	}
	
	private void handleOutput(String string) {
		if (document != null) {
			string.replaceAll("\r", "");
			Style style = document.getCurrentStyle();
			if (style != null) {
				int increase = 
						document.getCursorOffset() - 
						document.getLength() + 
						string.length();
				style.setLength(style.getLength() + increase);
			}
			document.replace(
					document.getCursorOffset(), 
					document.getLength() - document.getCursorOffset(), 
					string);
			document.moveCursorTo(document.getCursorOffset() + string.length());
		}
	}

}
