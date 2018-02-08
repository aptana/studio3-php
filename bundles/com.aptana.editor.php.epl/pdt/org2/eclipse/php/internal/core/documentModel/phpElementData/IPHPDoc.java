package org2.eclipse.php.internal.core.documentModel.phpElementData;

/**
 * A very generic description of a PHPDoc block.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPDoc
{
	String getShortDescription();

	String getLongDescription();

	IPHPDocTag[] getTags();
}
