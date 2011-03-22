/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org2.eclipse.dltk.compiler.problem;

import java.util.Locale;

import org.eclipse.core.resources.IResource;

public class DefaultProblemFactory implements IProblemFactory {
	private Locale locale;

	public DefaultProblemFactory() {
		this(Locale.getDefault());
	}

	public DefaultProblemFactory(Locale loc) {
		this.locale = loc;
		if (Locale.getDefault().equals(loc)) {
			// if (DEFAULT_LOCALE_TEMPLATES == null){
			// DEFAULT_LOCALE_TEMPLATES = loadMessageTemplates(loc);
			// }
			// this.messageTemplates = DEFAULT_LOCALE_TEMPLATES;
		} else {
			// this.messageTemplates = loadMessageTemplates(loc);
		}
	}

	public IProblem createProblem(String originatingFileName, int problemId,
			String[] problemArguments, String[] messageArguments, int severity,
			int startPosition, int endPosition, int lineNumber, int columnNumber) {

		String message = getLocalizedMessage(problemId, messageArguments);

		return new DefaultProblem(null, message, problemId, problemArguments,
				severity, startPosition, endPosition, lineNumber, columnNumber);
	}

	public Locale getLocale() {
		return locale;
	}

	public String getLocalizedMessage(int problemId, String[] messageArguments) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < messageArguments.length; i++) {
			b.append(messageArguments[i]);
			if (i != messageArguments.length - 1) {
				b.append(" "); //$NON-NLS-1$
			}
		}
		return b.toString();
	}

	public IProblemReporter createReporter(IResource resource) {

		return null;
		//return new DLTKProblemReporter(resource, this);
	}
}
