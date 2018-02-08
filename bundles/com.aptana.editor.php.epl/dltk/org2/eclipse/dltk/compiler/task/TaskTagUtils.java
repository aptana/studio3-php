/**
 * 
 */
package org2.eclipse.dltk.compiler.task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

@SuppressWarnings("deprecation")
public abstract class TaskTagUtils {

	private static final String TAG_SEPARATOR = ","; //$NON-NLS-1$
	private static final String PRIORITY_SEPARATOR = ";"; //$NON-NLS-1$

	public static List<TodoTask> decodeTaskTags(String tags) {
		final String[] tagPairs = getTokens(tags, TAG_SEPARATOR);
		final List<TodoTask> elements = new ArrayList<TodoTask>();
		for (int i = 0; i < tagPairs.length; ++i) {
			final String[] values = getTokens(tagPairs[i], PRIORITY_SEPARATOR);
			final TodoTask task = new TodoTask();
			task.name = values[0];
			if (values.length == 2) {
				task.priority = values[1];
			} else {
				task.priority = TodoTask.PRIORITY_NORMAL;
			}
			elements.add(task);
		}
		return elements;

	}

	public static String encodeTaskTags(List<TodoTask> elements) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < elements.size(); ++i) {
			final TodoTask task = elements.get(i);
			if (i > 0) {
				sb.append(TAG_SEPARATOR);
			}
			sb.append(task.name);
			sb.append(PRIORITY_SEPARATOR);
			sb.append(task.priority);
		}
		final String string = sb.toString();
		return string;
	}

	public static List<TodoTask> getDefaultTags() {
		final List<TodoTask> defaultTags = new ArrayList<TodoTask>();
		defaultTags.add(new TodoTask("FIXME", TodoTask.PRIORITY_HIGH)); //$NON-NLS-1$
		defaultTags.add(new TodoTask("OPTIMIZE", TodoTask.PRIORITY_NORMAL)); //$NON-NLS-1$
		defaultTags.add(new TodoTask("TODO", TodoTask.PRIORITY_NORMAL)); //$NON-NLS-1$
		defaultTags.add(new TodoTask("XXX", TodoTask.PRIORITY_NORMAL)); //$NON-NLS-1$
		return defaultTags;
	}

	@Deprecated
	public static void initializeDefaultValues(Preferences store) {
		store.setDefault(ITodoTaskPreferences.ENABLED, true);
		store.setDefault(ITodoTaskPreferences.CASE_SENSITIVE, true);
		store.setDefault(ITodoTaskPreferences.TAGS,
				encodeTaskTags(getDefaultTags()));
	}

	public static void initializeDefaultValues(IEclipsePreferences prefs) {
		prefs.putBoolean(ITodoTaskPreferences.ENABLED, true);
		prefs.putBoolean(ITodoTaskPreferences.CASE_SENSITIVE, true);
		prefs.put(ITodoTaskPreferences.TAGS, encodeTaskTags(getDefaultTags()));
	}

	public static boolean isValidName(String newText) {
		return newText.indexOf(TAG_SEPARATOR.charAt(0)) < 0
				&& newText.indexOf(PRIORITY_SEPARATOR.charAt(0)) < 0;
	}

	private static String[] getTokens(String text, String separator) {
		final StringTokenizer tok = new StringTokenizer(text, separator);
		final int nTokens = tok.countTokens();
		final String[] res = new String[nTokens];
		for (int i = 0; i < res.length; i++) {
			res[i] = tok.nextToken().trim();
		}
		return res;
	}
}