package com.aptana.editor.php.internal.ui.editor;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jface.text.IDocument;
import org2.eclipse.php.internal.core.PHPVersion;

/**
 * PHPVersion manager that keeps track of the opened documents and the PHPVersion that is used to color their content.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPVersionDocumentManager
{
	private static PHPVersionDocumentManager instance;
	private Map<IDocument, PHPVersion> map;
	private Map<IDocument, Integer> documentsCount;

	// Private constructor
	private PHPVersionDocumentManager()
	{
		map = new WeakHashMap<IDocument, PHPVersion>();
		documentsCount = new WeakHashMap<IDocument, Integer>();
	}

	// Returns a singleton instance
	private static PHPVersionDocumentManager getInstance()
	{
		if (instance == null)
		{
			instance = new PHPVersionDocumentManager();
		}
		return instance;
	}

	/**
	 * Returns the {@link PHPVersion} attached to the given {@link IDocument} instance.
	 * 
	 * @param document
	 * @return A {@link PHPVersion}.
	 */
	public static PHPVersion getPHPVersion(IDocument document)
	{
		return getInstance().map.get(document);
	}

	/**
	 * Attach, or update the attachment, for the {@link PHPVersion} used in the given {@link IDocument}.
	 * 
	 * @param document
	 * @param phpVersion
	 */
	public static void updateVersion(IDocument document, PHPVersion phpVersion)
	{
		PHPVersionDocumentManager manager = getInstance();
		manager.map.put(document, phpVersion);
	}

	/**
	 * Increase the count for the given document to keep track of the number of editors that use the same document.
	 * 
	 * @param document
	 * @see #decreaseDocumentCount(IDocument)
	 */
	public static void increaseDocumentCount(IDocument document)
	{
		PHPVersionDocumentManager manager = getInstance();
		Integer count = manager.documentsCount.get(document);
		if (count == null)
		{
			manager.documentsCount.put(document, 1);
		}
		else
		{
			manager.documentsCount.put(document, count + 1);
		}
	}

	/**
	 * Decrease the count for the given document. The decrease is called when an editor that uses this document is
	 * disposed. After the last tracked document is removed, the manager will return null for any consecutive call for
	 * {@link #getPHPVersion(IDocument)} with that document reference.
	 * 
	 * @param document
	 * @see #increaseDocumentCount(IDocument)
	 */
	public static void decreaseDocumentCount(IDocument document)
	{
		PHPVersionDocumentManager manager = getInstance();
		Integer count = manager.documentsCount.get(document);
		if (count == null || count == 1)
		{
			manager.map.remove(document);
		}
		else
		{
			manager.documentsCount.put(document, count - 1);
		}
	}
}
