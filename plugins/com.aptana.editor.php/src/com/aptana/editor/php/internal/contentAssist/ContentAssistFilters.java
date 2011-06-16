package com.aptana.editor.php.internal.contentAssist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org2.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ElementsIndexingUtils;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.IElementEntriesFilter;
import com.aptana.editor.php.internal.indexer.NamespacePHPEntryValue;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;

/**
 * A class that holds PHP content assist filters that are used when we calculate the proposals.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class ContentAssistFilters
{

	/**
	 * Filters entries by module and modules this module might include.
	 * 
	 * @param input
	 *            - input to filter.
	 * @param module
	 *            - module.
	 * @param index
	 *            - index to use.
	 * @return set of filtered entries.
	 */
	public static Set<IElementEntry> filterByModule(Collection<IElementEntry> input, IModule module,
			IElementsIndex index)
	{
		IElementEntriesFilter filter = ElementsIndexingUtils.createIncludeFilter(module, index);
		if (filter == null)
		{
			Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
			result.addAll(input);
			return result;
		}

		return filter.filter(input);
	}

	/**
	 * Gets the list of entries and returns only the static ones.
	 * 
	 * @param entries
	 *            - entries to filter.
	 * @return filtered entries.
	 */
	public static Set<IElementEntry> filterStaticEntries(Set<IElementEntry> entries)
	{
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();

		for (IElementEntry entry : entries)
		{
			if (entry.getCategory() == IPHPIndexConstants.CONST_CATEGORY)
			{
				result.add(entry);
			}
			else if ((entry.getCategory() == IPHPIndexConstants.VAR_CATEGORY || entry.getCategory() == IPHPIndexConstants.FUNCTION_CATEGORY)
					&& entry.getValue() instanceof AbstractPHPEntryValue)
			{

				AbstractPHPEntryValue val = (AbstractPHPEntryValue) entry.getValue();
				int modifiers = val.getModifiers();
				if (PHPFlags.isStatic(modifiers))
				{
					result.add(entry);
				}
			}
		}
		return result;
	}

	/**
	 * Gets the list of entries and returns only the non-static ones.
	 * 
	 * @param entries
	 *            - entries to filter.
	 * @return filtered entries.
	 */
	public static Set<IElementEntry> filterNonStaticVariables(Set<IElementEntry> entries)
	{
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		for (IElementEntry entry : entries)
		{
			if ((entry.getCategory() == IPHPIndexConstants.VAR_CATEGORY || entry.getCategory() == IPHPIndexConstants.FUNCTION_CATEGORY)
					&& entry.getValue() instanceof AbstractPHPEntryValue)
			{
				AbstractPHPEntryValue val = (AbstractPHPEntryValue) entry.getValue();
				int modifiers = val.getModifiers();
				if (!(PHPFlags.isStatic(modifiers) && entry.getCategory() == IPHPIndexConstants.VAR_CATEGORY))
				{
					result.add(entry);
				}
			}
		}
		return result;
	}

	/**
	 * Gets the list of entries, removes fields and methods and returns the result.
	 * 
	 * @param entries
	 *            - entries to filter.
	 * @return filtered entries.
	 */
	public static List<IElementEntry> filterFieldsAndMembers(List<IElementEntry> entries)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();

		for (IElementEntry entry : entries)
		{
			if (entry.getValue() instanceof VariablePHPEntryValue)
			{
				VariablePHPEntryValue value = (VariablePHPEntryValue) entry.getValue();
				if (!value.isField() || entry.getCategory() == IPHPIndexConstants.CONST_CATEGORY)
				{
					result.add(entry);
				}
			}
			else if (entry.getValue() instanceof FunctionPHPEntryValue)
			{
				if (!((FunctionPHPEntryValue) entry.getValue()).isMethod())
				{
					result.add(entry);
				}
			}
			else if (entry.getValue() instanceof ClassPHPEntryValue)
			{
				result.add(entry);
			}
			else if (entry.getValue() instanceof NamespacePHPEntryValue)
			{
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Filters global variables to remove those that are not imported.
	 * 
	 * @param entries
	 *            - entries.
	 * @param globalImports
	 *            - global imports set.
	 * @return filtered entries.
	 */
	public static List<IElementEntry> filterGlobalVariables(List<IElementEntry> entries, Set<String> globalImports)
	{
		if (globalImports == null)
		{
			return entries;
		}

		List<IElementEntry> result = new ArrayList<IElementEntry>();

		for (IElementEntry entry : entries)
		{
			if (entry.getValue() instanceof VariablePHPEntryValue)
			{

				VariablePHPEntryValue val = (VariablePHPEntryValue) entry.getValue();

				// if we got global variable
				if (!val.isField() && !val.isLocal() && !val.isParameter())
				{
					if (!globalImports.contains(ElementsIndexingUtils.getLastNameInPath(entry.getEntryPath())))
					{
						continue;
					}
				}
			}
			result.add(entry);
		}
		return result;
	}

	/**
	 * Gets the Set of entries, removes all but functions and methods.
	 * 
	 * @param entries
	 *            - entries to filter.
	 * @param index
	 * @return filtered entries.
	 */
	public static Set<IElementEntry> filterAllButFunctions(Set<IElementEntry> entries, IElementsIndex index)
	{
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();

		for (IElementEntry entry : entries)
		{
			Object value = entry.getValue();
			if (value instanceof FunctionPHPEntryValue)
			{
				result.add(entry);
			}
			if (value instanceof ClassPHPEntryValue)
			{
				String entryPath = entry.getEntryPath();
				List<IElementEntry> entries2 = index.getEntries(IPHPIndexConstants.FUNCTION_CATEGORY, entryPath
						+ IElementsIndex.DELIMITER + "__construct"); //$NON-NLS-1$
				for (IElementEntry e : entries2)
				{
					result.add(e);
				}
				if (entries2.isEmpty())
				{
					entries2 = index.getEntries(IPHPIndexConstants.FUNCTION_CATEGORY, entryPath
							+ IElementsIndex.DELIMITER + entryPath);
					for (IElementEntry e : entries2)
					{
						result.add(e);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Gets the Set of entries, removes all but classes.
	 * 
	 * @param entries
	 *            - entries to filter.
	 * @param index
	 * @return filtered entries.
	 */
	public static Set<IElementEntry> filterAllButClasses(Set<IElementEntry> entries, IElementsIndex index)
	{
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();

		for (IElementEntry entry : entries)
		{
			Object value = entry.getValue();
			if (value instanceof ClassPHPEntryValue)
			{
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Gets the Set of entries, removes all but variables, fields and classes.
	 * 
	 * @param entries
	 *            - entries to filter.
	 * @param index
	 * @return filtered entries.
	 */
	public static Set<IElementEntry> filterAllButVariablesAndClasses(Set<IElementEntry> entries, IElementsIndex index)
	{
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();

		for (IElementEntry entry : entries)
		{
			Object value = entry.getValue();
			if (value instanceof VariablePHPEntryValue || value instanceof ClassPHPEntryValue)
			{
				result.add(entry);
			}
		}
		return result;
	}
}
