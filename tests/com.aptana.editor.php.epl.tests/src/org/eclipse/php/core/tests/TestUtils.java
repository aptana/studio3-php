package org.eclipse.php.core.tests;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.preferences.CorePreferenceConstants;

import com.aptana.editor.php.epl.tests.Activator;

public class TestUtils
{
	private static String getDiffError(String expected, String actual, int expectedDiff, int actualDiff)
	{
		StringBuilder errorBuf = new StringBuilder();
		errorBuf.append("\nEXPECTED:\n--------------\n"); //$NON-NLS-1$
		errorBuf.append(expected.substring(0, expectedDiff)).append("*****").append(expected.substring(expectedDiff)); //$NON-NLS-1$
		errorBuf.append("\n\nACTUAL:\n--------------\n"); //$NON-NLS-1$
		errorBuf.append(actual.substring(0, actualDiff)).append("*****").append(actual.substring(actualDiff)); //$NON-NLS-1$
		return errorBuf.toString();
	}

	/**
	 * Compares expected result with the actual.
	 * 
	 * @param expected
	 * @param actual
	 * @return difference string or <code>null</code> in case expected result is equal to the actual.
	 */
	public static String compareContents(String expected, String actual)
	{
		actual = actual.replaceAll("[\r\n]+", "\n").trim(); //$NON-NLS-1$ //$NON-NLS-2$
		expected = expected.replaceAll("[\r\n]+", "\n").trim(); //$NON-NLS-1$ //$NON-NLS-2$

		int expectedDiff = StringUtils.indexOfDifference(actual, expected);
		if (expectedDiff >= 0)
		{
			int actualDiff = StringUtils.indexOfDifference(expected, actual);
			return getDiffError(expected, actual, expectedDiff, actualDiff);
		}
		return null;
	}

	/**
	 * Compares expected result with the actual ingoring whitespace characters
	 * 
	 * @param expected
	 * @param actual
	 * @return difference string or <code>null</code> in case expected result is equal to the actual.
	 */
	public static String compareContentsIgnoreWhitespace(String expected, String actual)
	{
		String tmpExpected = expected;
		String tmpActual = actual;
		String diff = StringUtils.difference(tmpExpected, tmpActual);
		while (diff.length() > 0)
		{
			String diff2 = StringUtils.difference(tmpActual, tmpExpected);

			if (!Character.isWhitespace(diff.charAt(0)) && !Character.isWhitespace(diff2.charAt(0)))
			{
				int expectedDiff = StringUtils.indexOfDifference(tmpActual, tmpExpected)
						+ (expected.length() - tmpExpected.length());
				int actualDiff = StringUtils.indexOfDifference(tmpExpected, tmpActual)
						+ (actual.length() - tmpActual.length());
				return getDiffError(expected, actual, expectedDiff, actualDiff);
			}

			tmpActual = diff.trim();
			tmpExpected = diff2.trim();

			diff = StringUtils.difference(tmpExpected, tmpActual);
		}
		return null;
	}

	public static void waitForIndexer()
	{
		// TODO
	}

	/**
	 * Wait for autobuild notification to occur, that is for the autbuild to finish.
	 * 
	 * @throws CoreException
	 */
	public static void waitForAutoBuild() throws CoreException
	{
		boolean wasInterrupted = false;
		do
		{
			try
			{
				if (!wasInterrupted)
				{
					throw new RuntimeException("Before joining the auto-build");
				}
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				wasInterrupted = false;
			}
			catch (OperationCanceledException e)
			{
				throw (e);
			}
			catch (InterruptedException e)
			{
				wasInterrupted = true;
			}
		}
		while (wasInterrupted);
	}

	/**
	 * Set project PHP version
	 * 
	 * @param project
	 * @param phpVersion
	 * @throws CoreException
	 */
	public static void setProjectPhpVersion(IProject project, PHPVersion phpVersion) throws CoreException
	{
		Preferences pref = getPreferences(project);
		if (pref != null)
		{
			if (!phpVersion.getAlias().equals(pref.get(CorePreferenceConstants.Keys.PHP_VERSION, null)))
			{
				pref.put(CorePreferenceConstants.Keys.PHP_VERSION, phpVersion.getAlias());
				try
				{
					pref.flush();
				}
				catch (BackingStoreException e)
				{
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
				}
				waitForAutoBuild();
				waitForIndexer();
			}
		}
	}

	protected static Preferences getPreferences(IProject project)
	{
		return new ProjectScope(project).getNode(Activator.PLUGIN_ID);
	}
}
