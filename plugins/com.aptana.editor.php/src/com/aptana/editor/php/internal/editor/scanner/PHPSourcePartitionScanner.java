package com.aptana.editor.php.internal.editor.scanner;

import com.aptana.editor.common.text.rules.SourceConfigurationPartitionScanner;
import com.aptana.editor.php.internal.editor.PHPSourceConfiguration;

/**
 * PHP source partitions scanner
 */
public class PHPSourcePartitionScanner extends SourceConfigurationPartitionScanner
{
	public PHPSourcePartitionScanner()
	{
		super(PHPSourceConfiguration.getDefault());
	}
}
