==PREFS==
php.formatter.spaces.after.case.colon=1
php.formatter.spaces.before.arrow=1
php.formatter.spaces.before.colon=0
php.formatter.indent.blocks=true
php.formatter.spaces.before.parentheses.closing=0
php.formatter.indent.class.body=true
php.formatter.spaces.before.staticInvocation=0
php.formatter.spaces.after.assignment=1
php.formatter.formatter.on=@formatter:on
php.formatter.spaces.before.declaration.parentheses.opening=0
php.formatter.spaces.before.invocation.parentheses.closing=0
php.formatter.spaces.after.namespaceSeparator=0
php.formatter.spaces.before.array.access.parentheses.closing=0
php.formatter.spaces.after.forSemicolon=1
php.formatter.spaces.after.staticInvocation=0
php.formatter.spaces.before.prefix=0
php.formatter.spaces.after.arithmetic=1
php.formatter.spaces.before.conditional.parentheses.closing=0
php.formatter.line.after.class.declaration=1
php.formatter.newline.before.else=false
php.formatter.line.after.function.declaration=1
php.formatter.spaces.after.conditional.parentheses.opening=0
php.formatter.spaces.after.parentheses=0
php.formatter.spaces.before.conditional=1
php.formatter.spaces.before.loop.parentheses.closing=0
php.formatter.spaces.before.conditional.parentheses.opening=1
php.formatter.formatter.off=@formatter:off
php.formatter.newline.before.dowhile=false
php.formatter.spaces.before.parentheses=0
php.formatter.brace.position.blocks=same.line
php.formatter.wrap.comments.length=80
php.formatter.indent.breakInCase=true
php.formatter.newline.between.array.creation.elements=false
php.formatter.newline.before.if.in.elseif=false
php.formatter.spaces.before.unary=0
php.formatter.formatter.on.off.enabled=false
php.formatter.spaces.after.arrow=1
php.formatter.spaces.after.unary=0
php.formatter.spaces.after.keyValue=1
php.formatter.spaces.before.keyValue=1
php.formatter.indent.switch.body=true
php.formatter.spaces.before.forSemicolon=0
php.formatter.wrap.comments=false
php.formatter.brace.position.class.declaration=same.line
php.formatter.spaces.after.semicolon=1
php.formatter.spaces.after.prefix=0
php.formatter.formatter.tabulation.char=editor
php.formatter.spaces.after.commas=1
php.formatter.indent.case.body=true
php.formatter.spaces.after.postfix=0
php.formatter.spaces.before.relational=1
php.formatter.spaces.before.postfix=0
php.formatter.indent.namespace.blocks=false
php.formatter.brace.position.case.block=same.line
php.formatter.spaces.before.array.access.parentheses.opening=0
php.formatter.spaces.before.namespaceSeparator=0
php.formatter.newline.before.catch=false
php.formatter.spaces.before.commas=0
php.formatter.spaces.after.colon=1
php.formatter.spaces.after.declaration.parentheses.opening=0
php.formatter.spaces.before.semicolon=0
php.formatter.formatter.tabulation.size=4
php.formatter.spaces.before.declaration.parentheses.closing=0
php.formatter.spaces.after.invocation.parentheses.opening=0
php.formatter.line.preserve=1
php.formatter.spaces.after.loop.parentheses.opening=0
php.formatter.formatter.indentation.size=4
php.formatter.indent.function.body=true
php.formatter.brace.position.switch.block=same.line
php.formatter.spaces.before.assignment=1
php.formatter.brace.position.function.declaration=same.line
php.formatter.spaces.before.loop.parentheses.opening=1
php.formatter.indent.php.body=false
php.formatter.spaces.before.case.colon=1
php.formatter.spaces.after.conditional=1
php.formatter.spaces.after.dot=1
php.formatter.spaces.after.relational=1
php.formatter.spaces.before.dot=1
php.formatter.spaces.before.arithmetic=1
php.formatter.spaces.before.invocation.parentheses.opening=0
php.formatter.spaces.after.array.access.parentheses.opening=0
==CONTENT==
<?php
if ( !defined("INIT_DONE") )
{
	print "Improper access! Exiting now...";
	exit();
}

/**
 * MYSQLi DRIVER
 *
 * @package  Audith CMS codename Persephone
 * @author   Shahriyar Imanov <shehi@imanov.name>
 * @version  1.0
**/
require_once( PATH_SOURCES . "/kernel/db.php" );
class Db__Drivers__Mysql extends Database
{
	/**
	 * Zend DB instance
	 * @var Zend_Db
	 */
	public $db;

	/**
	 * Constructor
	 *
	 * @param    Registry    REFERENCE: Registry Object
	 */
	public function __construct ( Registry $Registry )
	{
		$this->Registry = $Registry;

		$this->Registry->loader( "Zend_Db", false );

		# Db options
		$driver_options = array(
				PDO::MYSQL_ATTR_USE_BUFFERED_QUERY  => true,
				PDO::MYSQL_ATTR_INIT_COMMAND        => "SET NAMES UTF8;"
			);

		$options = array(
				Zend_Db::AUTO_QUOTE_IDENTIFIERS     => true
			);

		# Preparing DSN and Options for PEAR DB::connect
		$params = array(
				'host'            => &$this->Registry->config['sql.hostname'],
				'username'        => &$this->Registry->config['sql.user'],
				'password'        => &$this->Registry->config['sql.password'],
				'dbname'          => &$this->Registry->config['sql.dbname'],
				'driver_options'  => $driver_options,
				'options'         => $options
			);

		$this->db = Zend_Db::factory( "Pdo_Mysql", $params );

		# Db Profiler
		if ( IN_DEV )
		{
			$this->Registry->loader( "Zend_Db_Profiler_Firebug", false );
			$_profiler = new Zend_Db_Profiler_Firebug('All DB Queries');
			$_profiler->setEnabled( true );

			# Attach the profiler to Db Adapter
			$this->db->setProfiler( $_profiler );

			# Check connection
			$_connection = $this->db->getConnection();
			$this->Registry->logger__do_log( "Database: Connection " . ( $_connection !== false ? "successful!" : "failed!" ) , $_connection !== false ? "INFO" : "ERROR" );
		}
	}


	/**
	 * Build "IS NULL" and "IS NOT NULL" string
	 *
	 * @param     boolean     IS NULL flag
	 * @return    string      [Optional] SQL-formatted "IS NULL" or "IS NOT NULL" string
	 */
	public function build__is_null( $is_null = true )
	{
		return $is_null ? " IS NULL " : " IS NOT NULL ";
	}


	/**
	 * The last value generated in the scope of the current database connection [for Insert queries]
	 *
	 * @return   integer   LAST_INSERT_ID
	 */
	public function last_insert_id ()
	{
		return $this->db->lastInsertId();
	}


	/**
	 * Determines the referenced tables, and the count of referenced rows (latter is on-demand)
	 *
	 * @param     string   Referenced table name
	 * @param     array    Parameters containing information for querying referenced data statistics
	 *                     array( '_do_count' => true|false, 'referenced_column_name' => '<column_name>', 'value_to_check' => <key_to_check_against> )
	 *
	 * @return    array    Reference and possibly, data statistics information (row-count)
	 */
	public function check_for_references ( $referenced_table_name , $_params = array() )
	{
		//----------------------------------
		// Fetching reference information
		//----------------------------------

		$this->cur_query = array(
				'do'      =>  "select",
				'fields'  =>  array( "table_name" , "column_name" , "referenced_column_name"),
				'table'   =>  array( "information_schema.KEY_COLUMN_USAGE" ),
				'where'   =>  array(
						array( 'table_schema = ' . $this->quote( $this->Registry->config['sql.dbname'] ) ),
						array( 'referenced_table_name = ' . $this->quote( $referenced_table_name ) ),
					)
			);
		$reference_information = $this->simple_exec_query();

		//----------------------------------------
		// Fetching referenced data statistics
		//----------------------------------------

		if ( !empty( $_params ) and $_params['_do_count'] === true and !empty( $_params['referenced_column_name'] ) and !empty( $_params['value_to_check'] ) )
		{
			$_data_statistics = array();
			foreach ( $reference_information as $_r )
			{
				if ( $_r['referenced_column_name'] != $_params['referenced_column_name'] )
				{
					continue;
				}

				$this->cur_query = array(
						'do'      =>  "select_one",
						'fields'  =>  array( new Zend_Db_Expr( "count(*)" ) ),
						'table'   =>  $_r['table_name'],
						'where'   =>  $_r['table_name'] . "." . $_r['column_name'] . "=" .
							(
								is_int( $_params['value_to_check'] )
								?
								$this->quote( $_params['value_to_check'], "INTEGER" )
								:
								$this->quote( $_params['value_to_check'] )
							),
					);
				$_data_statistics[ $_r['table_name'] ] = $this->simple_exec_query();
			}
		}

		//----------
		// Return
		//----------

		return array( 'reference_information' => $reference_information, '_data_statistics' => $_data_statistics );
	}


	/**
	 * Prepares column-data for ALTER query for a given module data-field-type
	 *
	 * @param   array      Data-field info
	 * @param   boolean    Whether translated info will be applied to "_master_repo" tables or not (related to Connector-enabled fields only!)
	 * @return  array      Column info
	 */
	public function modules__ddl_column_type_translation ( $df_data , $we_need_this_for_master_table = false )
	{
		if ( $we_need_this_for_master_table === true and ( isset( $df_data['connector_enabled'] ) and $df_data['connector_enabled'] == '1' ) )
		{
			$_col_info = array(
					'type'     =>  "MEDIUMTEXT",
					'length'   =>  null,
					'default'  =>  "",
					'extra'    =>  null,
					'attribs'  =>  null,
					'is_null'  =>  false
				);
		}
		else
		{
			if ( $df_data['type'] == 'alphanumeric' )
			{
				//---------------------------
				// Alphanumeric : Mixed data
				//---------------------------

				if ( $df_data['subtype'] == 'string' )
				{
					# Default value
					if ( ! isset( $df_data['default_value'] ) or is_null( $df_data['default_value'] ) )
					{
						if ( $df_data['is_required'] )
						{
							$_default_value = "";
							$_is_null       = false;
						}
						if ( ! $df_data['is_required'] )
						{
							$_default_value = null;
							$_is_null       = true;
						}
					}
					else
					{
						$_default_value = $df_data['default_value'];
						$_is_null       = false;
					}


					# Continue...
					if ( $df_data['maxlength'] <= 255 )
					{
						$_col_info = array(
								'type'     =>  "VARCHAR",
								'length'   =>  $df_data['maxlength'],
								'default'  =>  $_default_value,
								'attribs'  =>  null,
								'is_null'  =>  $_is_null,
								'indexes'  =>  $df_data['is_unique'] ? array( 'UNIQUE' => true ) : array()
							);
					}
					elseif ( $df_data['maxlength'] <= 65535 )
					{
						$_col_info = array(
								'type'     =>  "TEXT",
								'length'   =>  null,
								'default'  =>  $_default_value,
								'attribs'  =>  null,
								'is_null'  =>  $_is_null
							);
					}
					elseif ( $df_data['maxlength'] <= 16777215 )
					{
						$_col_info = array(
								'type'     =>  "MEDIUMTEXT",
								'length'   =>  null,
								'default'  =>  $_default_value,
								'attribs'  =>  null,
								'is_null'  =>  $_is_null
							);
					}
					else
					{
						# Anything larger than 16 megabytes is not accepted through a regular input-form-fields
					}

				}

				//--------------------------
				// Alphanumeric : Integer
				//--------------------------

				elseif ( preg_match( '#^integer_(?P<attrib>(?:un)?signed)_(?P<bit_length>\d{1,2})$#', $df_data['subtype'], $_dft_subtype ) )
				{
					# Default value
					if ( ! isset( $df_data['default_value'] ) or is_null( $df_data['default_value'] ) )
					{
						if ( $df_data['is_required'] )
						{
							$_default_value = 0;
							$_is_null       = false;
						}
						if ( ! $df_data['is_required'] )
						{
							$_default_value = null;
							$_is_null       = true;
						}
					}
					else
					{
						$_default_value = intval( $df_data['default_value'] );
						$_is_null       = false;
					}

					$_col_info = array(
							'length'   =>  $df_data['maxlength'],
							'default'  =>  $_default_value,
							'attribs'  =>  ( $_dft_subtype['attrib'] == 'unsigned' ) ? "UNSIGNED" : "SIGNED",
							'is_null'  =>  $_is_null
						);

					# The rest...
					switch ( $_dft_subtype['bit_length'] )
					{
						case 8:
							$_col_info['type'] =  "TINYINT";
							break;

						case 16:
							$_col_info['type'] =  "SMALLINT";
							break;

						case 24:
							$_col_info['type'] =  "MEDIUMINT";
							break;

						case 32:
							$_col_info['type'] =  "INT";
							break;

						case 64:
							$_col_info['type'] =  "BIGINT";
							break;
					}
				}


				//--------------------------
				// Alphanumeric : Decimal
				//--------------------------

				elseif ( $df_data['subtype'] == 'decimal_signed' or $df_data['subtype'] == 'decimal_unsigned' )
				{
					# Default value
					if ( ! isset( $df_data['default_value'] ) or is_null( $df_data['default_value'] ) or ! is_numeric( $df_data['default_value'] ) )
					{
						if ( $df_data['is_required'] )
						{
							$_default_value = 0.00;
							$_is_null       = false;
						}
						if ( ! $df_data['is_required'] )
						{
							$_default_value = null;
							$_is_null       = true;
						}
					}
					else
					{
						$_default_value = floatval( $df_data['default_value'] );
						$_is_null       = false;
					}

					# The rest...
					$_col_info = array(
							'type'     =>  "DECIMAL",
							'length'   =>  $df_data['maxlength'],
							'default'  =>  $_default_value,
							'attribs'  =>  ( $df_data['subtype'] == 'decimal_unsigned' ) ? "UNSIGNED" : "SIGNED",
							'is_null'  =>  $_is_null,
						);
				}
				elseif ( in_array( $df_data['subtype'], array( "dropdown" , "multiple" ) ) )
				{
					# Default value
					if ( ! isset( $df_data['default_value'] ) or is_null( $df_data['default_value'] ) )
					{
						$_default_value = null;
						$_is_null       = true;
					}
					else
					{
						$_default_value = $df_data['default_value'];
						$_is_null       = false;
					}

					# The rest...
					switch ( $df_data['subtype'] )
					{
						case 'dropdown':
							$_col_info = array(
									'type'     =>  "ENUM",
									'length'   =>  $df_data['maxlength'],
									'default'  =>  $_default_value,
									'attribs'  =>  null,
									'is_null'  =>  $_is_null
								);
							break;

						case 'multiple':
							$_col_info = array(
									'type'     =>  "SET",
									'length'   =>  $df_data['maxlength'],
									'default'  =>  $_default_value,
									'attribs'  =>  null,
									'is_null'  =>  $_is_null
								);
							break;
					}
				}
			}
			elseif ( $df_data['type'] == 'file' )
			{
				//--------
				// File
				//--------

				# Files are represented with their 32-char-long MD5 checksum hashes
				$_col_info = array(
						'type'     =>  "VARCHAR",
						'length'   =>  32,
						'default'  =>  null,
						'attribs'  =>  null,
						'is_null'  =>  true
					);
			}
			elseif ( $df_data['type'] == 'link' )
			{
				//--------
				// Link
				//--------

				# All Links are references to 'id' column of module master tables
				$_col_info = array(
						'type'     =>  "INT",
						'length'   =>  10,
						'default'  =>  null,
						'attribs'  =>  null,
						'is_null'  =>  true
					);
			}
		}
		$_col_info['extra']   = null;

		$_col_info['name']    = $df_data['name'];

		$_col_info['comment'] = $df_data['label'];
		$_col_info['comment'] = html_entity_decode( $_col_info['comment'], ENT_QUOTES, "UTF-8" ); // Decoding characters
		$_col_info['comment'] = preg_replace( "/'{2,}/" , "'" , $_col_info['comment'] ); // Excessive single-quotes
		if ( mb_strlen( $_col_info['comment'] ) > 255 ) // Truncate here, as you might get trailing single-quotes later on, for comments with strlen() close to 255.
		{
			$_col_info['comment'] = mb_substr( $_col_info['comment'], 0, 255 );
		}
		$_col_info['comment'] = str_replace( "'", "''", $_col_info['comment'] ); // Single quotes are doubled in number. This is MySQL syntax!

		return $_col_info;
	}


	/**
	 * Returns the table structure for any of the module tables
	 *
	 * @param   array   Table suffix, determining specific table
	 * @return  array   Table structure
	 */
	public final function modules__default_table_structure ( $suffix )
	{
		$_struct['master_repo'] = array(
				'col_info'   => array(
						'id'      => array(
								'type'      => "int",
								'length'    => 10,
								'collation' => null,
								'attribs'   => "unsigned",
								'is_null'   => false,
								'default'   => null,
								'extra'     => "auto_increment",
								'indexes'   => array(
										"PRIMARY" => true
									)
							),
						'tags' => array(
								'type'      => "mediumtext",
								'length'    => null,
								'collation' => null,
								'attribs'   => null,
								'is_null'   => true,
								'default'   => null,
								'extra'     => null,
								'indexes'   => null
							),
						'timestamp' => array(
								'type'      => "int",
								'length'    => 10,
								'collation' => null,
								'attribs'   => "unsigned",
								'is_null'   => false,
								'default'   => null,
								'extra'     => null,
								'indexes'   => null
							),
						'submitted_by' => array(
								'type'      => "mediumint",
								'length'    => 8,
								'collation' => null,
								'attribs'   => "unsigned",
								'is_null'   => false,
								'default'   => 0,
								'extra'     => null,
								'indexes'   => null
							),
						'status_published' => array(
								'type'      => "tinyint",
								'length'    => 1,
								'collation' => null,
								'attribs'   => null,
								'is_null'   => false,
								'default'   => 0,
								'extra'     => null,
								'indexes'   => null
							),
						'status_locked' => array(
								'type'      => "tinyint",
								'length'    => 1,
								'collation' => null,
								'attribs'   => null,
								'is_null'   => false,
								'default'   => 0,
								'extra'     => null,
								'indexes'   => null
							),
						'_x_data_compatibility' => array(
								'type'      => "varchar",
								'length'    => 255,
								'collation' => null,
								'attribs'   => null,
								'is_null'   => true,
								'default'   => null,
								'extra'     => null,
								'indexes'   => null
							),
					),
				'comment'    => ""
			);

		$_struct['comments'] = array();  // @todo Comments

		$_struct['tags'] = array();  // @todo Tags

		$_struct['connector_repo'] = array(
				'col_info'   => array(
						'id' => array(
								'type'      => "int",
								'length'    => 10,
								'collation' => null,
								'attribs'   => "unsigned",
								'is_null'   => false,
								'default'   => null,
								'extra'     => "auto_increment",
								'indexes'   => array(
										'PRIMARY' => true
									),
							),
						'ref_id' => array(
								'type'      => "int",
								'length'    => 10,
								'collation' => null,
								'attribs'   => "unsigned",
								'is_null'   => false,
								'default'   => null,
								'extra'     => null,
							),
					),
			);

		return $_struct[ $suffix ];
	}


	/**
	 * Simple DELETE query
	 *
	 * @param      array       array( "do"=>"delete", "table"=>"" , "where"=>array() )
	 * @return     mixed       # of affected [deleted] rows on success, FALSE otherwise
	 */
	protected final function simple_delete_query ( $sql )
	{
		# "From"
		if ( !empty( $sql['table'] ) )
		{
			$table = $this->attach_prefix( $sql['table'] );
		}
		else
		{
			$this->Registry->loader( "Zend_Db_Exception", false );
			throw new Zend_Db_Exception("No or bad table references specified for DELETE query");
		}

		# "Where"
		$where = array();
		if ( isset( $sql['where'] ) )
		{
			# array-of-strings VS just a plain string
			if ( !is_array( $sql['where'] ) and !empty( $sql['where'] ) )
			{
				$where[] = $sql['where'];
			}
			elseif ( is_array( $sql['where'] ) and count( $sql['where'] ) )
			{
				$where = $sql['where'];
			}
		}
		if ( count( $where ) )
		{
			try
			{
				$return = $this->db->delete( $table, $where );
			}
			catch ( Zend_Db_Exception $e )
			{
				$this->Registry->exception_handler( $e );
				return false;
			}
			return $return;
		}
		else
		{
			try
			{
				$return = $this->db->query( "TRUNCATE TABLE " . $table )->rowCount();
			}
			catch ( Zend_Db_Exception $e )
			{
				$this->Registry->exception_handler( $e );
				return false;
			}
			return $return;
		}
	}



	/**
	 * Simple INSERT query
	 *
	 * @param     array      array( "do"=>"insert", "table"=>"", "set"=>array() )
	 * @return    integer    # of affected rows
	 */
	protected function simple_insert_query ( $sql )
	{
		# "Into"
		if ( !empty( $sql['table'] ) )
		{
			$table = $this->attach_prefix( $sql['table'] );
		}
		else
		{
			$this->Registry->loader( "Zend_Db_Exception", false );
			throw new Zend_Db_Exception("No or bad table references specified for INSERT query");
		}

		# Data
		if ( isset( $sql['set'] ) and is_array( $sql['set'] ) and count( $sql['set'] ) )
		{
			$data =& $sql['set'];
		}
		else
		{
			$this->Registry->loader( "Zend_Db_Exception", false );
			throw new Zend_Db_Exception("No data specified for SETting in INSERT query");
		}

		# EXEC
		try
		{
			return $this->db->insert( $table, $data );
		}
		catch ( Zend_Db_Exception $e )
		{
			$this->Registry->exception_handler( $e );
			return false;
		}
	}


	/**
	 * Simple REPLACE query
	 *
	 * @param   array    array( "do"=>"replace", "table"=>"", "set"=>array( associative array of column_name => value pairs , ... , ... ) )
	 * @return  mixed    # of affected rows on success, FALSE otherwise
	 */
	protected final function simple_replace_query ( $sql )
	{
		# "Into"
		if ( isset( $sql['table'] ) and ! empty( $sql['table'] ) )
		{
			$table = $this->attach_prefix( $sql['table'] );
		}
		else
		{
			$this->Registry->loader( "Zend_Db_Exception", false );
			throw new Zend_Db_Exception("No or bad table references specified for REPLACE query");
		}

		# "SET"
		if ( ! isset( $sql['set'] ) or ! is_array( $sql['set'] ) or ! count( $sql['set'] ) )
		{
			$this->Registry->loader( "Zend_Db_Exception", false );
			throw new Zend_Db_Exception("No data specified for SETting in REPLACE query");
			return false;
		}
		$_set = array();
		foreach ( $sql['set'] as $_col => $_val )
		{
			if ( $_val instanceof Zend_Db_Expr )
			{
				$_val = $_val->__toString();
				unset( $sql['set'][ $_col ] );
			}
			else
			{
				$_val = "?";
			}
			$_set[] = $this->db->quoteIdentifier( $_col, true ) . ' = ' . $_val;
		}

		//------------------------------
		// Build the REPLACE statement
		//------------------------------

		$this->cur_query = "REPLACE INTO " . $table . " SET " . implode( ", " , $_set );

		//----------------------------------------------------------------
		// Execute the statement and return the number of affected rows
		//----------------------------------------------------------------

		try
		{
			$stmt = $this->db->query( $this->cur_query, array_values( $sql['set'] ) );
			$result = $stmt->rowCount();
			return $result;
		}
		catch ( Zend_Db_Exception $e )
		{
			$this->Registry->exception_handler( $e );
			return false;
		}
	}


	/**
	 * Simple SELECT query
	 *
	 * @param    array    array(
	 							"do"          => "select",
								"distinct"    => TRUE | FALSE,           - enables you to add the DISTINCT  keyword to your SQL query
								"fields"      => array(),
								"table"       => array() [when correlation names are used] | string,
								"where"       => "" | array( array() ),  - multidimensional array, containing conditions and possible parameters for placeholders
								"add_join"    => array(
										0 => array (
											"fields"      => array(),
											"table"       => array(),    - where count = 1
											"conditions"  => "",
											"join_type"   => "INNER|CROSS|LEFT|RIGHT|NATURAL"
												"
										),
										1 => array()
									),
								"group"       => array(),
								"having"      => array(),
								"order"       => array(),
								"limit"       => array(offset, count),
								"limit_page"  => array(page, count)
							)
	 * @return    mixed     Result set
	 */
	protected final function simple_select_query ( $sql )
	{
		$select = $this->db->select();
		$this->Registry->loader( "Zend_Db_Select_Exception", false );

		# Columns
		$fields = array();
		if ( isset( $sql['fields'] ) and is_array( $sql['fields'] ) and count( $sql['fields'] ) )
		{
			$fields = $sql['fields'];
		}
		else
		{
			$fields = "*";
		}

		# "From"
		$tables = array();
		if ( isset( $sql['table'] ) and ( is_array( $sql['table'] ) and count( $sql['table'] ) ) or ( !is_array( $sql['table'] ) and !empty( $sql['table'] ) ) )
		{
			$table = $this->attach_prefix( $sql['table'] );
		}
		else
		{
			throw new Zend_Db_Select_Exception("No or bad table references specified for SELECT query");
		}
		if ( isset( $sql['distinct'] ) and $sql['distinct'] === true )
		{
			$select = $select->distinct();
		}
		$select = $select->from( $table, $fields );

		# "Where"
		$where = array();
		if ( isset( $sql['where'] ) )
		{
			# Backward compatibility
			if ( ! is_array( $sql['where'] ) )
			{
				$where = array_merge( $where, array( array( $sql['where'] ) ) );
			}
			else
			{
				if ( count( $sql['where'] ) )
				{
					$where = array_merge( $where, $sql['where'] );                             // $where_clauses can consist of clause alone, or clause-parameter pairs
				}
			}
		}
		if ( count( $where ) )                                                                 // Apply only if there is a need
		{
			foreach ( $where as $_w )
			{
				if ( isset( $_w[1] ) )
				{
					$select = $select->where( $_w[0], $_w[1] );
				}
				else
				{
					$select = $select->where( $_w[0] );
				}
			}
		}

		# "Join"
		if ( isset( $sql['add_join'] ) and count( $sql['add_join'] ) )
		{
			foreach ( $sql['add_join'] as $add_join )
			{
				$join_table       = array();
				$join_conditions  = array();
				$join_fields      = array();

				# "Join" table
				if ( isset( $add_join['table'] ) )
				{
					if ( is_array( $add_join['table'] ) and count( $add_join['table'] ) )
					{
						$join_table = array_merge( $join_table, $this->attach_prefix( $add_join['table'] ) );
					}
					else
					{
						$join_table = array_merge( $join_table, array( $this->attach_prefix( $add_join['table'] ) ) );
					}
				}
				else
				{
					throw new Zend_Db_Select_Exception("No table references specified for JOIN clause in SELECT query");
					continue;                                                                  // Failed "Join", continue to the next one...
				}

				# "Join" conditions
				if ( isset( $add_join['conditions'] ) and !empty( $add_join['conditions'] ) )
				{
					$join_conditions = $add_join['conditions'];
				}
				else
				{
					if ( $add_join['join_type'] != 'CROSS' and $add_join['join_type'] != 'NATURAL' )
					{
						throw new Zend_Db_Select_Exception("No conditions specified for JOIN clause in SELECT query");
						continue;                                                              // Failed "Join", continue to the next one...
					}
				}

				# "Join" fields
				if ( isset( $add_join['fields'] ) and $add_join['fields'] )
				{
					if ( is_array( $add_join['fields'] ) )
					{
						if ( count( $add_join['fields'] ) )
						{
							$join_fields = $add_join['fields'];
						}
						else
						{
							$join_fields = array();
						}
					}
					else
					{
						$join_fields = array( $add_join['fields'] );
					}
				}
				else
				{
					$join_fields = array();
				}

				# "Join" finalize...

				switch ( $add_join['join_type'] )
				{
					case 'INNER':
						$select = $select->joinInner( $join_table, $join_conditions, $join_fields );
						break;

					case 'CROSS':
						$select = $select->joinCross( $join_table, $join_fields );             // The joinCross() method has no parameter to specify the join condition
						break;

					case 'LEFT':
						$select = $select->joinLeft( $join_table, $join_conditions, $join_fields );
						break;

					case 'RIGHT':
						$select = $select->joinRight( $join_table, $join_conditions, $join_fields );
						break;

					case 'NATURAL':
						$select = $select->joinNatural( $join_table, $join_fields );           //  The joinNatural() method has no parameter to specify the join condition.
						break;

					default:
						$select = $select->join( $join_table, $join_conditions, $join_fields );
						break;
				}
			}
		}

		# "Group By"
		$group   = array();
		$having  = array();
		if ( isset( $sql['group'] ) )
		{
			if ( is_array( $sql['group'] ) and count( $sql['group'] ) )
			{
				$group = $sql['group'];
			}
			else
			{
				$group = array( $group );
			}

			# "Having"
			if ( isset( $sql['having'] ) )
			{
				if ( is_array( $sql['having'] ) and count( $sql['having'] ) )
				{
					$having = $sql['having'];
				}
				else
				{
					$having = array( $having );
				}
			}
		}
		if ( count( $group ) )                                                                 // Apply only if there is a need
		{
			$select = $select->group( $group );
		}
		if ( count( $having ) )                                                                // Apply only if there is a need
		{
			foreach ( $having as $_h )
			{
				if ( isset( $_h[1] ) and !empty( $_h[1] ) )
				{
					$select = $select->having( $_h[0], $_h[1] );
				}
				else
				{
					$select = $select->having( $_h[0] );
				}
			}
		}

		# "Order By"
		$order = array();
		if ( isset( $sql['order'] ) and is_array( $sql['order'] ) and count( $sql['order'] ) )
		{
			$order = $sql['order'];
		}
		if ( count( $order ) )                                                                 // Apply only if there is a need
		{
			$select = $select->order( $order );
		}

		# "Limit"
		if ( $sql['do'] == 'select_row' )
		{
			$select = $select->limit( 1, 0 );
		}
		elseif ( isset( $sql['limit'] ) and is_array( $sql['limit'] ) and count( $sql['limit'] ) == 2 )
		{
			$select = $select->limit( $sql['limit'][1], $sql['limit'][0] );
		}

		# "LimitPage"
		if ( isset( $sql['limit_page'] ) and is_array( $sql['limit_page'] ) and count( $sql['limit_page'] ) == 2 )
		{
			$select = $select->limitPage( $sql['limit_page'][0], $sql['limit_page'][1] );
		}

		# EXEC
		$this->db->setFetchMode( Zend_Db::FETCH_ASSOC );
		$statement = $this->db->query( $select );

		switch( $sql['do'] )
		{
			case 'select':
				try
				{
					$return = $statement->fetchAll();
				}
				catch ( Zend_Db_Select_Exception $e )
				{
					$this->Registry->exception_handler( $e );
					return false;
				}
				return $return;
				break;

			case 'select_row':
				try
				{
					$return = $statement->fetch();
				}
				catch ( Zend_Db_Select_Exception $e )
				{
					$this->Registry->exception_handler( $e );
					return false;
				}
				return $return;
				break;

			case 'select_one':
				try
				{
					$return = $statement->fetchColumn();
				}
				catch ( Zend_Db_Select_Exception $e )
				{
					$this->Registry->exception_handler( $e );
					return false;
				}
				return $return;
				break;

			default:
				throw new Zend_Db_Select_Exception("Invalid mode provided for SELECT query operation");
				break;
		}
	}


	/**
	 * Simple UPDATE query [w/ MULTITABLE UPDATE support]
	 *
	 * @param    array    array(
	 							"do"          => "update",
								"tables"      => mixed array [elements can be key=>value pairs ("table aliases") or strings],
								"set"         => assoc array of column_name-value pairs
								"where"       => array of strings | string
							)
	 * @return   mixed      # of rows affected on success, FALSE otherwise
	 */
	protected final function simple_update_query ( $sql )
	{
		//----------
		// Tables
		//----------

		$_tables = array();
		if ( !isset( $sql['tables'] ) )
		{
			return false;
		}
		if ( !is_array( $sql['tables'] ) )
		{
			$sql['tables'] = array( $sql['tables'] );
		}
		if ( !count( $sql['tables'] ) )
		{
			return false;
		}
		foreach ( $sql['tables'] as $_table )
		{
			# If "table name aliases" are used
			if ( is_array( $_table ) and count( $_table ) )
			{
				foreach ( $_table as $_alias=>$_table_name )
				{
					if ( is_numeric( $_alias ) )
					{
						$_tables[] = $this->db->quoteIdentifier( $this->attach_prefix( $_table_name ), true );
					}
					else
					{
						$_tables[] = $this->db->quoteIdentifier( $this->attach_prefix( $_table_name ), true )
							. " AS "
							. $this->db->quoteIdentifier( $_alias, true );
					}
				}
			}
			# If its just an array of strings - i.e. no "table name aliases"
			else
			{
				$_tables[] = $this->db->quoteIdentifier( $this->attach_prefix( $_table ), true );
			}
		}

		//---------
		// "SET"
		//---------

		if ( !count( $sql['set'] ) )
		{
			return false;
		}
		$_set = array();
		foreach ( $sql['set'] as $_col => $_val )
		{
			if ( $_val instanceof Zend_Db_Expr )
			{
				$_val = $_val->__toString();
				unset( $sql['set'][ $_col ] );
			}
			else
			{
				$_val = "?";
			}
			$_set[] = $this->db->quoteIdentifier( $_col, true ) . ' = ' . $_val;
		}

		//-----------
		// "WHERE"
		//-----------

		$_where = ! is_array( $sql['where'] ) ? array( $sql['where'] ) : $sql['where'];

		foreach ($_where as $_cond => &$_term) {
            # is $_cond an int? (i.e. Not a condition)
            if ( is_int( $_cond ) ) {
                # $_term is the full condition
                if ( $_term instanceof Zend_Db_Expr ) {
                    $_term = $_term->__toString();
                }
            } else {
                # $_cond is the condition with placeholder,
                # and $_term is quoted into the condition
                $_term = $this->db->quoteInto( $_cond, $_term );
            }
            $_term = '(' . $_term . ')';
        }

        $_where = implode(' AND ', $_where);

		//------------------------------
		// Build the UPDATE statement
		//------------------------------

		$this->cur_query = "UPDATE "
			. implode( ", " , $_tables )
			. " SET " . implode( ", " , $_set )
			. ( $_where ? " WHERE " . $_where : "" );

		//----------------------------------------------------------------
		// Execute the statement and return the number of affected rows
		//----------------------------------------------------------------

		try
		{
			$stmt = $this->db->query( $this->cur_query, array_values( $sql['set'] ) );
			$result = $stmt->rowCount();
			return $result;
		}
		catch ( Zend_Db_Exception $e )
		{
			$this->Registry->exception_handler( $e );
			return false;
		}
	}


	/**
	 * Simple ALTER TABLE query
	 *
	 * @param    array      array(
	 							"do"          => "alter",
								"table"       => string,
								"action"      => "add_column"|"drop_column"|"change_column"|"add_key"
								"col_info"    => column info to parse
							)
	 * @return   mixed      # of affected rows on success, FALSE otherwise
	 */
	protected function simple_alter_table ( $sql )
	{
		if ( ! $sql['table'] )
		{
			return false;
		}

		# Let's clean-up COMMENT a bit
		if ( isset( $sql['comment'] ) )
		{
			$sql['comment'] = html_entity_decode( $sql['comment'], ENT_QUOTES, "UTF-8" ); // Decoding characters
			$sql['comment'] = preg_replace( "/'{2,}/" , "'" , $sql['comment'] ); // Excessive single-quotes
			if ( mb_strlen( $sql['comment'] ) > 60 ) // Truncate here, as you might get trailing single-quotes later on, for comments with strlen() close to 60.
			{
				$sql['comment'] = mb_substr( $sql['comment'], 0, 60 );
			}
			$sql['comment'] = str_replace( "'", "''", $sql['comment'] ); // Single quotes are doubled in number. This is MySQL syntax!
		}
		else
		{
			$sql['comment'] = "";
		}

		$this->cur_query = "ALTER TABLE " . $this->db->quoteIdentifier( $this->attach_prefix( $sql['table'] ) ) . " ";

		switch ( $sql['action'] )
		{
			//----------------
			// "ADD COLUMN"
			//----------------

			case 'add_column':
				$this->cur_query .= "ADD ";
				switch ( strtolower( $sql['col_info']['type'] ) )
				{
					case 'tinyint':
					case 'smallint':
					case 'mediumint':
					case 'int':
					case 'bigint':
					case 'float':
					case 'double':
					case 'decimal':
						$this->cur_query .=
							"`" . $sql['col_info']['name'] . "` "
							. $sql['col_info']['type']
							. ( $sql['col_info']['length']           ? "(" . $sql['col_info']['length'] . ")" : "" )
							. " " . $sql['col_info']['attribs']
							. ( $sql['col_info']['is_null']          ? " NULL" : " NOT NULL" )
							// . ( $sql['col_info']['extra']            ? " " . $sql['col_info']['extra'] : "" )
							. ( $sql['col_info']['default'] !== null ? " DEFAULT '" . $sql['col_info']['default'] . "'" : " DEFAULT NULL" )
							. ( $sql['col_info']['comment'] ? " COMMENT '" . $sql['col_info']['comment'] . "'" : "" );
						break;

					case 'varchar':
					case 'char':
					case 'tinytext':
					case 'mediumtext':
					case 'text':
					case 'longtext':
						$this->cur_query .=
							"`" . $sql['col_info']['name'] . "` "
							. $sql['col_info']['type']
							. ( $sql['col_info']['length']           ? "(" . $sql['col_info']['length'] . ")" : "" )
							. " collate utf8_unicode_ci"
							. " " . $sql['col_info']['attribs']
							. ( $sql['col_info']['is_null']          ? " NULL" : " NOT NULL" )
							// . ( $sql['col_info']['extra']            ? " " . $sql['col_info']['extra'] : "" )
							. ( $sql['col_info']['default'] !== null ? " DEFAULT '" . $sql['col_info']['default'] . "'" : " DEFAULT NULL" )
							. ( $sql['col_info']['comment'] ? " COMMENT '" . $sql['col_info']['comment'] . "'" : "" );
						break;
					case 'enum':
					case 'set':
						$this->cur_query .=
							"`" . $sql['col_info']['name'] . "` "
							. $sql['col_info']['type']
							. ( $sql['col_info']['length']           ? "(" . $sql['col_info']['length'] . ")" : "" )
							. " collate utf8_unicode_ci"
							. ( $sql['col_info']['is_null']          ? " NULL" : " NOT NULL" )
							. ( $sql['col_info']['default'] !== null ? " DEFAULT '" . $sql['col_info']['default'] . "'" : " DEFAULT NULL" )
							. ( $sql['col_info']['comment'] ? " COMMENT '" . $sql['col_info']['comment'] . "'" : "" );
						break;
				}

				# KEYs

				if ( isset( $sql['col_info']['indexes'] ) and is_array( $sql['col_info']['indexes'] ) and count( $sql['col_info']['indexes'] ) )
				{
					foreach ( $sql['col_info']['indexes'] as $k => $v )
					{
						if ( $v === true )
						{
							$this->cur_query .= ", ADD " . $k . " (" . $sql['col_info']['name'] . ")";
						}
					}
				}
				break;

			case 'drop_column':
				$i = 0;
				foreach ( $sql['col_info'] as $col_name=>$col_info )
				{
					$this->cur_query .= ( ( $i == 0 ) ? "DROP `" : ", DROP `" ) . $col_info['name'] . "` ";
					$i++;
				}
				break;

			case 'change_column':
				$i = 0;
				foreach ( $sql['col_info'] as $col_name=>$col_info )
				{
					$this->cur_query .= ( ( $i == 0 ) ? "CHANGE `" : ", CHANGE `" ) . $col_info['old_name'] . "` ";
					switch ( strtolower( $col_info['type'] ) )
					{
						case 'tinyint':
						case 'smallint':
						case 'mediumint':
						case 'int':
						case 'bigint':
						case 'float':
						case 'double':
						case 'decimal':
							$this->cur_query .=
								"`" . $col_info['name'] . "` "
								. $col_info['type']
								. ( $col_info['length']           ? "(" . $col_info['length'] . ")" : "" )
								. " " . $col_info['attribs']
								. ( $col_info['is_null']          ? " NULL" : " NOT NULL" )
								// . ( $col_info['extra']            ? " " . $col_info['extra'] : "" )
								. ( $col_info['default'] !== null ? " DEFAULT '" . $col_info['default'] . "'" : " DEFAULT NULL" )
								. ( $col_info['comment'] ? " COMMENT '" . $col_info['comment'] . "'" : "" );
							break;

						case 'varchar':
						case 'char':
						case 'tinytext':
						case 'mediumtext':
						case 'text':
						case 'longtext':
							$this->cur_query .=
								"`" . $col_info['name'] . "` "
								. $col_info['type']
								. ( $col_info['length']           ? "(" . $col_info['length'] . ")" : "" )
								. " collate utf8_unicode_ci"
								. " " . $col_info['attribs']
								. ( $col_info['is_null']          ? " NULL" : " NOT NULL" )
								// . ( $col_info['extra']            ? " " . $col_info['extra'] : "" )
								. ( $col_info['default'] !== null ? " DEFAULT '" . $col_info['default'] . "'" : " DEFAULT NULL" )
								. ( $col_info['comment'] ? " COMMENT '" . $col_info['comment'] . "'" : "" );
							break;
						case 'enum':
						case 'set':
							$this->cur_query .=
								"`" . $col_info['name'] . "` "
								. $col_info['type']
								. ( $col_info['length']           ? "(" . $col_info['length'] . ")" : "" )
								. " collate utf8_unicode_ci"
								. ( $col_info['is_null']          ? " NULL" : " NOT NULL" )
								. ( $col_info['default'] !== null ? " DEFAULT '" . $col_info['default'] . "'" : " DEFAULT NULL" )
								. ( $col_info['comment'] ? " COMMENT '" . $col_info['comment'] . "'" : "" );
							break;
					}

					# KEYs

					if ( isset( $col_info['indexes'] ) and is_array( $col_info['indexes'] ) and count( $col_info['indexes'] ) )
					{
						foreach ( $col_info['indexes'] as $k => $v )
						{
							if ( $v === true )
							{
								$this->cur_query .= ", ADD " . $k . " (" . $col_info['name'] . ")";
							}
						}
					}

					$i++;
				}
				break;

			//-------------
			// "COMMENT"
			//-------------

			case 'comment':
				$this->cur_query .= " COMMENT '" . $sql['comment'] . "'";
				break;
		}

		try
		{
			$stmt = new Zend_Db_Statement_Pdo( $this->db, $this->cur_query );
			return $stmt->execute();
		}
		catch ( Zend_Db_Exception $e )
		{
			$this->Registry->exception_handler( $e );
			return false;
		}
	}


	/**
	 * Drops table(s)
	 *
	 * @param    array     List of tables to be dropped
	 * @return   mixed     # of affected rows on success, FALSE otherwise
	 */
	public function simple_exec_drop_table ( $tables )
	{
		//-----------------
		// Build and exec
		//-----------------

		if ( !is_array( $tables ) )
		{
			return false;
		}
		else
		{
			if ( !count( $tables ) )
			{
				return false;
			}
			else
			{
				$tables = $this->attach_prefix( $tables );
				foreach ( $tables as &$table )
				{
					$table = $this->db->quoteIdentifier( $table, true );
				}
				$this->cur_query = "DROP TABLE IF EXISTS " . implode( ", " , $tables );
			}
		}

		try
		{
			$stmt = new Zend_Db_Statement_Pdo( $this->db, $this->cur_query );
			$this->query_count++;
			return $stmt->execute();
		}
		catch ( Zend_Db_Exception $e )
		{
			$this->Registry->exception_handler( $e );
			return false;
		}
	}


	/**
	 * Builds "CREATE TABLE ..." query from Table-Structure Array and executes it
	 *
	 * @param    array     Struct array
	 * @return   integer   # of queries executed
	 */
	public function simple_exec_create_table_struct ( $struct )
	{
		if ( ! count( $struct ) )
		{
			return 0;
		}

		//-----------------
		// Build and exec
		//-----------------

		$i = 0;
		$q_count = 0;
		foreach ( $struct['tables'] as $table=>$data )
		{
			//------------------------
			// DROP TABLE IF EXISTS
			//------------------------

			$this->simple_exec_drop_table( array( $table ) );

			//--------------------------------
			// Build CREATE TABLE statement
			//--------------------------------

			$this->cur_query = "CREATE TABLE IF NOT EXISTS " . $this->db->quoteIdentifier( $this->attach_prefix( $table ), true ) . " (\n";

			# CREATE TABLE...
			# Reset KEY data
			$key = array();

			foreach ( $data['col_info'] as $_column_name=>$_column_struct )
			{
				# Handling columns
				switch ( strtolower( $_column_struct['type'] ) )
				{
					case 'tinyint':
					case 'smallint':
					case 'mediumint':
					case 'int':
					case 'bigint':
					case 'float':
					case 'double':
					case 'decimal':
						$this->cur_query .=
							$this->db->quoteIdentifier( $_column_name, true ) . " "
							. $_column_struct['type']
							. ( $_column_struct['length']           ? "(" . $_column_struct['length'] . ")" : "" )
							. " " . $_column_struct['attribs']
							. ( $_column_struct['is_null']          ? " NULL" : " NOT NULL" )
							. ( $_column_struct['extra']            ? " " . $_column_struct['extra'] : "" )
							. ( $_column_struct['default'] !== null ? " DEFAULT '" . $_column_struct['default'] . "'" : "" )
							. ",\n";
						break;
					case 'varchar':
					case 'char':
					case 'tinytext':
					case 'mediumtext':
					case 'text':
					case 'longtext':
						$this->cur_query .=
							$this->db->quoteIdentifier( $_column_name, true ) . " "
							. $_column_struct['type']
							. ( $_column_struct['length']           ? "(" . $_column_struct['length'] . ")" : "" )
							. " collate utf8_unicode_ci"
							. " " . $_column_struct['attribs']
							. ( $_column_struct['is_null']          ? " NULL" : " NOT NULL" )
							. ( $_column_struct['extra']            ? " " . $_column_struct['extra'] : "" )
							. ( $_column_struct['default'] !== null ? " DEFAULT '" . $_column_struct['default'] . "'" : "" )
							. ",\n";
						break;
				}

				# Handling KEYs
				if ( isset( $_column_struct['indexes'] ) and is_array( $_column_struct['indexes'] ) and count( $_column_struct['indexes'] >= 1 ) )
				{
					foreach ( $_column_struct['indexes'] as $k=>$v )
					{
						if ( $v === true )
						{
							$key[] = $k . " KEY "
								. $this->db->quoteIdentifier( $_column_name, true )
								. "(" . $this->db->quoteIdentifier( $_column_name, true ) . ")";
						}
						# $v is index_type, e.g. "USING {BTREE | HASH}"
						else
						{
							$key[] = $k . " KEY "
								. $v
								. " " . $this->db->quoteIdentifier( $_column_name, true )
								. "(" . $this->db->quoteIdentifier( $_column_name, true ) . ")";
						}
					}
				}
			}

			# Attaching KEYs to the rest of query
			$this->cur_query .= implode( ",\n" , $key ) . "\n";

			# Finalizing CREATE TABLE ... query
			$data['storage_engine'] = ( isset( $data['storage_engine'] ) and !empty( $data['storage_engine'] ) ) ? $data['storage_engine'] : "MyISAM";
			$data['charset'] = ( isset( $data['charset'] ) and !empty( $data['charset'] ) ) ? $data['charset'] : "utf8";
			$data['collate'] = ( isset( $data['collate'] ) and !empty( $data['collate'] ) ) ? $data['collate'] : "utf8_unicode_ci";

			# Let's clean-up COMMENT a bit
			if ( isset( $data['comment'] ) )
			{
				$data['comment'] = html_entity_decode( $data['comment'], ENT_QUOTES, "UTF-8" ); // Decoding characters
				$data['comment'] = preg_replace( "/'{2,}/" , "'" , $data['comment'] ); // Excessive single-quotes
				if ( mb_strlen( $data['comment'] ) > 60 ) // Truncate here, as you might get trailing single-quotes later on, for comments with strlen() close to 60.
				{
					$data['comment'] = mb_substr( $data['comment'], 0, 60 );
				}
				$data['comment'] = str_replace( "'", "''", $data['comment'] ); // Single quotes are doubled in number. This is MySQL syntax!
			}
			else
			{
				$data['comment'] = "";
			}

			$this->cur_query .= ") ENGINE=" . $data['storage_engine'] . " DEFAULT CHARACTER SET=" . $data['charset'] . " COLLATE=" . $data['collate'] . " COMMENT='" . $data['comment'] . "';\n\n";

			# Execute
			$return = 0;
			try
			{
				$stmt = new Zend_Db_Statement_Pdo( $this->db, $this->cur_query );
				$this->query_count++;
				return $stmt->execute();
			}
			catch ( Zend_Db_Exception $e )
			{
				$this->Registry->exception_handler( $e );
				return false;
			}
		}

		return $return;
	}
}
==FORMATTED==

if (!defined("INIT_DONE")) {
	print "Improper access! Exiting now...";
	exit();
}

/**
 * MYSQLi DRIVER
 *
 * @package  Audith CMS codename Persephone
 * @author   Shahriyar Imanov <shehi@imanov.name>
 * @version  1.0
 **/
require_once (PATH_SOURCES . "/kernel/db.php");
class Db__Drivers__Mysql extends Database {
	/**
	 * Zend DB instance
	 * @var Zend_Db
	 */
	public $db;

	/**
	 * Constructor
	 *
	 * @param    Registry    REFERENCE: Registry Object
	 */
	public function __construct(Registry $Registry) {
		$this -> Registry = $Registry;

		$this -> Registry -> loader("Zend_Db", false);

		# Db options
		$driver_options = array(PDO::MYSQL_ATTR_USE_BUFFERED_QUERY => true, PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES UTF8;");

		$options = array(Zend_Db::AUTO_QUOTE_IDENTIFIERS => true);

		# Preparing DSN and Options for PEAR DB::connect
		$params = array('host' => &$this -> Registry -> config['sql.hostname'], 'username' => &$this -> Registry -> config['sql.user'], 'password' => &$this -> Registry -> config['sql.password'], 'dbname' => &$this -> Registry -> config['sql.dbname'], 'driver_options' => $driver_options, 'options' => $options);

		$this -> db = Zend_Db::factory("Pdo_Mysql", $params);

		# Db Profiler
		if (IN_DEV) {
			$this -> Registry -> loader("Zend_Db_Profiler_Firebug", false);
			$_profiler = new Zend_Db_Profiler_Firebug('All DB Queries');
			$_profiler -> setEnabled(true);

			# Attach the profiler to Db Adapter
			$this -> db -> setProfiler($_profiler);

			# Check connection
			$_connection = $this -> db -> getConnection();
			$this -> Registry -> logger__do_log("Database: Connection " . ($_connection !== false ? "successful!" : "failed!"), $_connection !== false ? "INFO" : "ERROR");
		}
	}

	/**
	 * Build "IS NULL" and "IS NOT NULL" string
	 *
	 * @param     boolean     IS NULL flag
	 * @return    string      [Optional] SQL-formatted "IS NULL" or "IS NOT NULL" string
	 */
	public function build__is_null($is_null = true) {
		return $is_null ? " IS NULL " : " IS NOT NULL ";
	}

	/**
	 * The last value generated in the scope of the current database connection [for Insert queries]
	 *
	 * @return   integer   LAST_INSERT_ID
	 */
	public function last_insert_id() {
		return $this -> db -> lastInsertId();
	}

	/**
	 * Determines the referenced tables, and the count of referenced rows (latter is on-demand)
	 *
	 * @param     string   Referenced table name
	 * @param     array    Parameters containing information for querying referenced data statistics
	 *                     array( '_do_count' => true|false, 'referenced_column_name' => '<column_name>', 'value_to_check' => <key_to_check_against> )
	 *
	 * @return    array    Reference and possibly, data statistics information (row-count)
	 */
	public function check_for_references($referenced_table_name, $_params = array()) {
		//----------------------------------
		// Fetching reference information
		//----------------------------------

		$this -> cur_query = array('do' => "select", 'fields' => array("table_name", "column_name", "referenced_column_name"), 'table' => array("information_schema.KEY_COLUMN_USAGE"), 'where' => array( array('table_schema = ' . $this -> quote($this -> Registry -> config['sql.dbname'])), array('referenced_table_name = ' . $this -> quote($referenced_table_name)), ));
		$reference_information = $this -> simple_exec_query();

		//----------------------------------------
		// Fetching referenced data statistics
		//----------------------------------------

		if (!empty($_params) and $_params['_do_count'] === true and !empty($_params['referenced_column_name']) and !empty($_params['value_to_check'])) {
			$_data_statistics = array();
			foreach ($reference_information as $_r) {
				if ($_r['referenced_column_name'] != $_params['referenced_column_name']) {
					continue;
				}

				$this -> cur_query = array('do' => "select_one", 'fields' => array(new Zend_Db_Expr("count(*)")), 'table' => $_r['table_name'], 'where' => $_r['table_name'] . "." . $_r['column_name'] . "=" . (is_int($_params['value_to_check']) ? $this -> quote($_params['value_to_check'], "INTEGER") : $this -> quote($_params['value_to_check'])), );
				$_data_statistics[$_r['table_name']] = $this -> simple_exec_query();
			}
		}

		//----------
		// Return
		//----------

		return array('reference_information' => $reference_information, '_data_statistics' => $_data_statistics);
	}

	/**
	 * Prepares column-data for ALTER query for a given module data-field-type
	 *
	 * @param   array      Data-field info
	 * @param   boolean    Whether translated info will be applied to "_master_repo" tables or not (related to Connector-enabled fields only!)
	 * @return  array      Column info
	 */
	public function modules__ddl_column_type_translation($df_data, $we_need_this_for_master_table = false) {
		if ($we_need_this_for_master_table === true and (isset($df_data['connector_enabled']) and $df_data['connector_enabled'] == '1')) {
			$_col_info = array('type' => "MEDIUMTEXT", 'length' => null, 'default' => "", 'extra' => null, 'attribs' => null, 'is_null' => false);
		} else {
			if ($df_data['type'] == 'alphanumeric') {
				//---------------------------
				// Alphanumeric : Mixed data
				//---------------------------

				if ($df_data['subtype'] == 'string') {
					# Default value
					if (!isset($df_data['default_value']) or is_null($df_data['default_value'])) {
						if ($df_data['is_required']) {
							$_default_value = "";
							$_is_null = false;
						}
						if (!$df_data['is_required']) {
							$_default_value = null;
							$_is_null = true;
						}
					} else {
						$_default_value = $df_data['default_value'];
						$_is_null = false;
					}

					# Continue...
					if ($df_data['maxlength'] <= 255) {
						$_col_info = array('type' => "VARCHAR", 'length' => $df_data['maxlength'], 'default' => $_default_value, 'attribs' => null, 'is_null' => $_is_null, 'indexes' => $df_data['is_unique'] ? array('UNIQUE' => true) : array());
					} elseif ($df_data['maxlength'] <= 65535) {
						$_col_info = array('type' => "TEXT", 'length' => null, 'default' => $_default_value, 'attribs' => null, 'is_null' => $_is_null);
					} elseif ($df_data['maxlength'] <= 16777215) {
						$_col_info = array('type' => "MEDIUMTEXT", 'length' => null, 'default' => $_default_value, 'attribs' => null, 'is_null' => $_is_null);
					} else {
						# Anything larger than 16 megabytes is not accepted through a regular input-form-fields
					}

				}

				//--------------------------
				// Alphanumeric : Integer
				//--------------------------

				elseif (preg_match('#^integer_(?P<attrib>(?:un)?signed)_(?P<bit_length>\d{1,2})$#', $df_data['subtype'], $_dft_subtype)) {
					# Default value
					if (!isset($df_data['default_value']) or is_null($df_data['default_value'])) {
						if ($df_data['is_required']) {
							$_default_value = 0;
							$_is_null = false;
						}
						if (!$df_data['is_required']) {
							$_default_value = null;
							$_is_null = true;
						}
					} else {
						$_default_value = intval($df_data['default_value']);
						$_is_null = false;
					}

					$_col_info = array('length' => $df_data['maxlength'], 'default' => $_default_value, 'attribs' => ($_dft_subtype['attrib'] == 'unsigned') ? "UNSIGNED" : "SIGNED", 'is_null' => $_is_null);

					# The rest...
					switch ( $_dft_subtype['bit_length'] ) {
						case 8 :
							$_col_info['type'] = "TINYINT";
							break;

						case 16 :
							$_col_info['type'] = "SMALLINT";
							break;

						case 24 :
							$_col_info['type'] = "MEDIUMINT";
							break;

						case 32 :
							$_col_info['type'] = "INT";
							break;

						case 64 :
							$_col_info['type'] = "BIGINT";
							break;
					}
				}

				//--------------------------
				// Alphanumeric : Decimal
				//--------------------------

				elseif ($df_data['subtype'] == 'decimal_signed' or $df_data['subtype'] == 'decimal_unsigned') {
					# Default value
					if (!isset($df_data['default_value']) or is_null($df_data['default_value']) or !is_numeric($df_data['default_value'])) {
						if ($df_data['is_required']) {
							$_default_value = 0.00;
							$_is_null = false;
						}
						if (!$df_data['is_required']) {
							$_default_value = null;
							$_is_null = true;
						}
					} else {
						$_default_value = floatval($df_data['default_value']);
						$_is_null = false;
					}

					# The rest...
					$_col_info = array('type' => "DECIMAL", 'length' => $df_data['maxlength'], 'default' => $_default_value, 'attribs' => ($df_data['subtype'] == 'decimal_unsigned') ? "UNSIGNED" : "SIGNED", 'is_null' => $_is_null, );
				} elseif (in_array($df_data['subtype'], array("dropdown", "multiple"))) {
					# Default value
					if (!isset($df_data['default_value']) or is_null($df_data['default_value'])) {
						$_default_value = null;
						$_is_null = true;
					} else {
						$_default_value = $df_data['default_value'];
						$_is_null = false;
					}

					# The rest...
					switch ( $df_data['subtype'] ) {
						case 'dropdown' :
							$_col_info = array('type' => "ENUM", 'length' => $df_data['maxlength'], 'default' => $_default_value, 'attribs' => null, 'is_null' => $_is_null);
							break;

						case 'multiple' :
							$_col_info = array('type' => "SET", 'length' => $df_data['maxlength'], 'default' => $_default_value, 'attribs' => null, 'is_null' => $_is_null);
							break;
					}
				}
			} elseif ($df_data['type'] == 'file') {
				//--------
				// File
				//--------

				# Files are represented with their 32-char-long MD5 checksum hashes
				$_col_info = array('type' => "VARCHAR", 'length' => 32, 'default' => null, 'attribs' => null, 'is_null' => true);
			} elseif ($df_data['type'] == 'link') {
				//--------
				// Link
				//--------

				# All Links are references to 'id' column of module master tables
				$_col_info = array('type' => "INT", 'length' => 10, 'default' => null, 'attribs' => null, 'is_null' => true);
			}
		}
		$_col_info['extra'] = null;

		$_col_info['name'] = $df_data['name'];

		$_col_info['comment'] = $df_data['label'];
		$_col_info['comment'] = html_entity_decode($_col_info['comment'], ENT_QUOTES, "UTF-8");
		// Decoding characters
		$_col_info['comment'] = preg_replace("/'{2,}/", "'", $_col_info['comment']);
		// Excessive single-quotes
		if (mb_strlen($_col_info['comment']) > 255)// Truncate here, as you might get trailing single-quotes later on, for comments with strlen() close to 255.
		{
			$_col_info['comment'] = mb_substr($_col_info['comment'], 0, 255);
		}
		$_col_info['comment'] = str_replace("'", "''", $_col_info['comment']);
		// Single quotes are doubled in number. This is MySQL syntax!

		return $_col_info;
	}

	/**
	 * Returns the table structure for any of the module tables
	 *
	 * @param   array   Table suffix, determining specific table
	 * @return  array   Table structure
	 */
	public final function modules__default_table_structure($suffix) {
		$_struct['master_repo'] = array('col_info' => array('id' => array('type' => "int", 'length' => 10, 'collation' => null, 'attribs' => "unsigned", 'is_null' => false, 'default' => null, 'extra' => "auto_increment", 'indexes' => array("PRIMARY" => true)), 'tags' => array('type' => "mediumtext", 'length' => null, 'collation' => null, 'attribs' => null, 'is_null' => true, 'default' => null, 'extra' => null, 'indexes' => null), 'timestamp' => array('type' => "int", 'length' => 10, 'collation' => null, 'attribs' => "unsigned", 'is_null' => false, 'default' => null, 'extra' => null, 'indexes' => null), 'submitted_by' => array('type' => "mediumint", 'length' => 8, 'collation' => null, 'attribs' => "unsigned", 'is_null' => false, 'default' => 0, 'extra' => null, 'indexes' => null), 'status_published' => array('type' => "tinyint", 'length' => 1, 'collation' => null, 'attribs' => null, 'is_null' => false, 'default' => 0, 'extra' => null, 'indexes' => null), 'status_locked' => array('type' => "tinyint", 'length' => 1, 'collation' => null, 'attribs' => null, 'is_null' => false, 'default' => 0, 'extra' => null, 'indexes' => null), '_x_data_compatibility' => array('type' => "varchar", 'length' => 255, 'collation' => null, 'attribs' => null, 'is_null' => true, 'default' => null, 'extra' => null, 'indexes' => null), ), 'comment' => "");

		$_struct['comments'] = array();
		// @todo Comments

		$_struct['tags'] = array();
		// @todo Tags

		$_struct['connector_repo'] = array('col_info' => array('id' => array('type' => "int", 'length' => 10, 'collation' => null, 'attribs' => "unsigned", 'is_null' => false, 'default' => null, 'extra' => "auto_increment", 'indexes' => array('PRIMARY' => true), ), 'ref_id' => array('type' => "int", 'length' => 10, 'collation' => null, 'attribs' => "unsigned", 'is_null' => false, 'default' => null, 'extra' => null, ), ), );

		return $_struct[$suffix];
	}

	/**
	 * Simple DELETE query
	 *
	 * @param      array       array( "do"=>"delete", "table"=>"" , "where"=>array() )
	 * @return     mixed       # of affected [deleted] rows on success, FALSE otherwise
	 */
	protected final function simple_delete_query($sql) {
		# "From"
		if (!empty($sql['table'])) {
			$table = $this -> attach_prefix($sql['table']);
		} else {
			$this -> Registry -> loader("Zend_Db_Exception", false);
			throw new Zend_Db_Exception("No or bad table references specified for DELETE query");
		}

		# "Where"
		$where = array();
		if (isset($sql['where'])) {
			# array-of-strings VS just a plain string
			if (!is_array($sql['where']) and !empty($sql['where'])) {
				$where[] = $sql['where'];
			} elseif (is_array($sql['where']) and count($sql['where'])) {
				$where = $sql['where'];
			}
		}
		if (count($where)) {
			try {
				$return = $this -> db -> delete($table, $where);
			} catch ( Zend_Db_Exception $e ) {
				$this -> Registry -> exception_handler($e);
				return false;
			}
			return $return;
		} else {
			try {
				$return = $this -> db -> query("TRUNCATE TABLE " . $table) -> rowCount();
			} catch ( Zend_Db_Exception $e ) {
				$this -> Registry -> exception_handler($e);
				return false;
			}
			return $return;
		}
	}

	/**
	 * Simple INSERT query
	 *
	 * @param     array      array( "do"=>"insert", "table"=>"", "set"=>array() )
	 * @return    integer    # of affected rows
	 */
	protected function simple_insert_query($sql) {
		# "Into"
		if (!empty($sql['table'])) {
			$table = $this -> attach_prefix($sql['table']);
		} else {
			$this -> Registry -> loader("Zend_Db_Exception", false);
			throw new Zend_Db_Exception("No or bad table references specified for INSERT query");
		}

		# Data
		if (isset($sql['set']) and is_array($sql['set']) and count($sql['set'])) {
			$data = &$sql['set'];
		} else {
			$this -> Registry -> loader("Zend_Db_Exception", false);
			throw new Zend_Db_Exception("No data specified for SETting in INSERT query");
		}

		# EXEC
		try {
			return $this -> db -> insert($table, $data);
		} catch ( Zend_Db_Exception $e ) {
			$this -> Registry -> exception_handler($e);
			return false;
		}
	}

	/**
	 * Simple REPLACE query
	 *
	 * @param   array    array( "do"=>"replace", "table"=>"", "set"=>array( associative array of column_name => value pairs , ... , ... ) )
	 * @return  mixed    # of affected rows on success, FALSE otherwise
	 */
	protected final function simple_replace_query($sql) {
		# "Into"
		if (isset($sql['table']) and !empty($sql['table'])) {
			$table = $this -> attach_prefix($sql['table']);
		} else {
			$this -> Registry -> loader("Zend_Db_Exception", false);
			throw new Zend_Db_Exception("No or bad table references specified for REPLACE query");
		}

		# "SET"
		if (!isset($sql['set']) or !is_array($sql['set']) or !count($sql['set'])) {
			$this -> Registry -> loader("Zend_Db_Exception", false);
			throw new Zend_Db_Exception("No data specified for SETting in REPLACE query");
			return false;
		}
		$_set = array();
		foreach ($sql['set'] as $_col => $_val) {
			if ($_val instanceof Zend_Db_Expr) {
				$_val = $_val -> __toString();
				unset($sql['set'][$_col]);
			} else {
				$_val = "?";
			}
			$_set[] = $this -> db -> quoteIdentifier($_col, true) . ' = ' . $_val;
		}

		//------------------------------
		// Build the REPLACE statement
		//------------------------------

		$this -> cur_query = "REPLACE INTO " . $table . " SET " . implode(", ", $_set);

		//----------------------------------------------------------------
		// Execute the statement and return the number of affected rows
		//----------------------------------------------------------------

		try {
			$stmt = $this -> db -> query($this -> cur_query, array_values($sql['set']));
			$result = $stmt -> rowCount();
			return $result;
		} catch ( Zend_Db_Exception $e ) {
			$this -> Registry -> exception_handler($e);
			return false;
		}
	}

	/**
	 * Simple SELECT query
	 *
	 * @param    array    array(
	 "do"          => "select",
	 "distinct"    => TRUE | FALSE,           - enables you to add the DISTINCT  keyword to your SQL query
	 "fields"      => array(),
	 "table"       => array() [when correlation names are used] | string,
	 "where"       => "" | array( array() ),  - multidimensional array, containing conditions and possible parameters for placeholders
	 "add_join"    => array(
	 0 => array (
	 "fields"      => array(),
	 "table"       => array(),    - where count = 1
	 "conditions"  => "",
	 "join_type"   => "INNER|CROSS|LEFT|RIGHT|NATURAL"
	 "
	 ),
	 1 => array()
	 ),
	 "group"       => array(),
	 "having"      => array(),
	 "order"       => array(),
	 "limit"       => array(offset, count),
	 "limit_page"  => array(page, count)
	 )
	 * @return    mixed     Result set
	 */
	protected final function simple_select_query($sql) {
		$select = $this -> db -> select();
		$this -> Registry -> loader("Zend_Db_Select_Exception", false);

		# Columns
		$fields = array();
		if (isset($sql['fields']) and is_array($sql['fields']) and count($sql['fields'])) {
			$fields = $sql['fields'];
		} else {
			$fields = "*";
		}

		# "From"
		$tables = array();
		if (isset($sql['table']) and (is_array($sql['table']) and count($sql['table'])) or (!is_array($sql['table']) and !empty($sql['table']))) {
			$table = $this -> attach_prefix($sql['table']);
		} else {
			throw new Zend_Db_Select_Exception("No or bad table references specified for SELECT query");
		}
		if (isset($sql['distinct']) and $sql['distinct'] === true) {
			$select = $select -> distinct();
		}
		$select = $select -> from($table, $fields);

		# "Where"
		$where = array();
		if (isset($sql['where'])) {
			# Backward compatibility
			if (!is_array($sql['where'])) {
				$where = array_merge($where, array( array($sql['where'])));
			} else {
				if (count($sql['where'])) {
					$where = array_merge($where, $sql['where']);
					// $where_clauses can consist of clause alone, or clause-parameter pairs
				}
			}
		}
		if (count($where))// Apply only if there is a need
		{
			foreach ($where as $_w) {
				if (isset($_w[1])) {
					$select = $select -> where($_w[0], $_w[1]);
				} else {
					$select = $select -> where($_w[0]);
				}
			}
		}

		# "Join"
		if (isset($sql['add_join']) and count($sql['add_join'])) {
			foreach ($sql['add_join'] as $add_join) {
				$join_table = array();
				$join_conditions = array();
				$join_fields = array();

				# "Join" table
				if (isset($add_join['table'])) {
					if (is_array($add_join['table']) and count($add_join['table'])) {
						$join_table = array_merge($join_table, $this -> attach_prefix($add_join['table']));
					} else {
						$join_table = array_merge($join_table, array($this -> attach_prefix($add_join['table'])));
					}
				} else {
					throw new Zend_Db_Select_Exception("No table references specified for JOIN clause in SELECT query");
					continue;
					// Failed "Join", continue to the next one...
				}

				# "Join" conditions
				if (isset($add_join['conditions']) and !empty($add_join['conditions'])) {
					$join_conditions = $add_join['conditions'];
				} else {
					if ($add_join['join_type'] != 'CROSS' and $add_join['join_type'] != 'NATURAL') {
						throw new Zend_Db_Select_Exception("No conditions specified for JOIN clause in SELECT query");
						continue;
						// Failed "Join", continue to the next one...
					}
				}

				# "Join" fields
				if (isset($add_join['fields']) and $add_join['fields']) {
					if (is_array($add_join['fields'])) {
						if (count($add_join['fields'])) {
							$join_fields = $add_join['fields'];
						} else {
							$join_fields = array();
						}
					} else {
						$join_fields = array($add_join['fields']);
					}
				} else {
					$join_fields = array();
				}

				# "Join" finalize...

				switch ( $add_join['join_type'] ) {
					case 'INNER' :
						$select = $select -> joinInner($join_table, $join_conditions, $join_fields);
						break;

					case 'CROSS' :
						$select = $select -> joinCross($join_table, $join_fields);
						// The joinCross() method has no parameter to specify the join condition
						break;

					case 'LEFT' :
						$select = $select -> joinLeft($join_table, $join_conditions, $join_fields);
						break;

					case 'RIGHT' :
						$select = $select -> joinRight($join_table, $join_conditions, $join_fields);
						break;

					case 'NATURAL' :
						$select = $select -> joinNatural($join_table, $join_fields);
						//  The joinNatural() method has no parameter to specify the join condition.
						break;

					default :
						$select = $select -> join($join_table, $join_conditions, $join_fields);
						break;
				}
			}
		}

		# "Group By"
		$group = array();
		$having = array();
		if (isset($sql['group'])) {
			if (is_array($sql['group']) and count($sql['group'])) {
				$group = $sql['group'];
			} else {
				$group = array($group);
			}

			# "Having"
			if (isset($sql['having'])) {
				if (is_array($sql['having']) and count($sql['having'])) {
					$having = $sql['having'];
				} else {
					$having = array($having);
				}
			}
		}
		if (count($group))// Apply only if there is a need
		{
			$select = $select -> group($group);
		}
		if (count($having))// Apply only if there is a need
		{
			foreach ($having as $_h) {
				if (isset($_h[1]) and !empty($_h[1])) {
					$select = $select -> having($_h[0], $_h[1]);
				} else {
					$select = $select -> having($_h[0]);
				}
			}
		}

		# "Order By"
		$order = array();
		if (isset($sql['order']) and is_array($sql['order']) and count($sql['order'])) {
			$order = $sql['order'];
		}
		if (count($order))// Apply only if there is a need
		{
			$select = $select -> order($order);
		}

		# "Limit"
		if ($sql['do'] == 'select_row') {
			$select = $select -> limit(1, 0);
		} elseif (isset($sql['limit']) and is_array($sql['limit']) and count($sql['limit']) == 2) {
			$select = $select -> limit($sql['limit'][1], $sql['limit'][0]);
		}

		# "LimitPage"
		if (isset($sql['limit_page']) and is_array($sql['limit_page']) and count($sql['limit_page']) == 2) {
			$select = $select -> limitPage($sql['limit_page'][0], $sql['limit_page'][1]);
		}

		# EXEC
		$this -> db -> setFetchMode(Zend_Db::FETCH_ASSOC);
		$statement = $this -> db -> query($select);

		switch( $sql['do'] ) {
			case 'select' :
				try {
					$return = $statement -> fetchAll();
				} catch ( Zend_Db_Select_Exception $e ) {
					$this -> Registry -> exception_handler($e);
					return false;
				}
				return $return;
				break;

			case 'select_row' :
				try {
					$return = $statement -> fetch();
				} catch ( Zend_Db_Select_Exception $e ) {
					$this -> Registry -> exception_handler($e);
					return false;
				}
				return $return;
				break;

			case 'select_one' :
				try {
					$return = $statement -> fetchColumn();
				} catch ( Zend_Db_Select_Exception $e ) {
					$this -> Registry -> exception_handler($e);
					return false;
				}
				return $return;
				break;

			default :
				throw new Zend_Db_Select_Exception("Invalid mode provided for SELECT query operation");
				break;
		}
	}

	/**
	 * Simple UPDATE query [w/ MULTITABLE UPDATE support]
	 *
	 * @param    array    array(
	 "do"          => "update",
	 "tables"      => mixed array [elements can be key=>value pairs ("table aliases") or strings],
	 "set"         => assoc array of column_name-value pairs
	 "where"       => array of strings | string
	 )
	 * @return   mixed      # of rows affected on success, FALSE otherwise
	 */
	protected final function simple_update_query($sql) {
		//----------
		// Tables
		//----------

		$_tables = array();
		if (!isset($sql['tables'])) {
			return false;
		}
		if (!is_array($sql['tables'])) {
			$sql['tables'] = array($sql['tables']);
		}
		if (!count($sql['tables'])) {
			return false;
		}
		foreach ($sql['tables'] as $_table) {
			# If "table name aliases" are used
			if (is_array($_table) and count($_table)) {
				foreach ($_table as $_alias => $_table_name) {
					if (is_numeric($_alias)) {
						$_tables[] = $this -> db -> quoteIdentifier($this -> attach_prefix($_table_name), true);
					} else {
						$_tables[] = $this -> db -> quoteIdentifier($this -> attach_prefix($_table_name), true) . " AS " . $this -> db -> quoteIdentifier($_alias, true);
					}
				}
			}
			# If its just an array of strings - i.e. no "table name aliases"
			else {
				$_tables[] = $this -> db -> quoteIdentifier($this -> attach_prefix($_table), true);
			}
		}

		//---------
		// "SET"
		//---------

		if (!count($sql['set'])) {
			return false;
		}
		$_set = array();
		foreach ($sql['set'] as $_col => $_val) {
			if ($_val instanceof Zend_Db_Expr) {
				$_val = $_val -> __toString();
				unset($sql['set'][$_col]);
			} else {
				$_val = "?";
			}
			$_set[] = $this -> db -> quoteIdentifier($_col, true) . ' = ' . $_val;
		}

		//-----------
		// "WHERE"
		//-----------

		$_where = !is_array($sql['where']) ? array($sql['where']) : $sql['where'];

		foreach ($_where as $_cond => &$_term) {
			# is $_cond an int? (i.e. Not a condition)
			if (is_int($_cond)) {
				# $_term is the full condition
				if ($_term instanceof Zend_Db_Expr) {
					$_term = $_term -> __toString();
				}
			} else {
				# $_cond is the condition with placeholder,
				# and $_term is quoted into the condition
				$_term = $this -> db -> quoteInto($_cond, $_term);
			}
			$_term = '(' . $_term . ')';
		}

		$_where = implode(' AND ', $_where);

		//------------------------------
		// Build the UPDATE statement
		//------------------------------

		$this -> cur_query = "UPDATE " . implode(", ", $_tables) . " SET " . implode(", ", $_set) . ($_where ? " WHERE " . $_where : "");

		//----------------------------------------------------------------
		// Execute the statement and return the number of affected rows
		//----------------------------------------------------------------

		try {
			$stmt = $this -> db -> query($this -> cur_query, array_values($sql['set']));
			$result = $stmt -> rowCount();
			return $result;
		} catch ( Zend_Db_Exception $e ) {
			$this -> Registry -> exception_handler($e);
			return false;
		}
	}

	/**
	 * Simple ALTER TABLE query
	 *
	 * @param    array      array(
	 "do"          => "alter",
	 "table"       => string,
	 "action"      => "add_column"|"drop_column"|"change_column"|"add_key"
	 "col_info"    => column info to parse
	 )
	 * @return   mixed      # of affected rows on success, FALSE otherwise
	 */
	protected function simple_alter_table($sql) {
		if (!$sql['table']) {
			return false;
		}

		# Let's clean-up COMMENT a bit
		if (isset($sql['comment'])) {
			$sql['comment'] = html_entity_decode($sql['comment'], ENT_QUOTES, "UTF-8");
			// Decoding characters
			$sql['comment'] = preg_replace("/'{2,}/", "'", $sql['comment']);
			// Excessive single-quotes
			if (mb_strlen($sql['comment']) > 60)// Truncate here, as you might get trailing single-quotes later on, for comments with strlen() close to 60.
			{
				$sql['comment'] = mb_substr($sql['comment'], 0, 60);
			}
			$sql['comment'] = str_replace("'", "''", $sql['comment']);
			// Single quotes are doubled in number. This is MySQL syntax!
		} else {
			$sql['comment'] = "";
		}

		$this -> cur_query = "ALTER TABLE " . $this -> db -> quoteIdentifier($this -> attach_prefix($sql['table'])) . " ";

		switch ( $sql['action'] ) {
			//----------------
			// "ADD COLUMN"
			//----------------

			case 'add_column' :
				$this -> cur_query .= "ADD ";
				switch ( strtolower( $sql['col_info']['type'] ) ) {
					case 'tinyint' :
					case 'smallint' :
					case 'mediumint' :
					case 'int' :
					case 'bigint' :
					case 'float' :
					case 'double' :
					case 'decimal' :
						$this -> cur_query .= "`" . $sql['col_info']['name'] . "` " . $sql['col_info']['type'] . ($sql['col_info']['length'] ? "(" . $sql['col_info']['length'] . ")" : "") . " " . $sql['col_info']['attribs'] . ($sql['col_info']['is_null'] ? " NULL" : " NOT NULL")
						// .  ( $sql['col_info']['extra']            ? " " . $sql['col_info']['extra'] : "" )
						. ($sql['col_info']['default'] !== null ? " DEFAULT '" . $sql['col_info']['default'] . "'" : " DEFAULT NULL") . ($sql['col_info']['comment'] ? " COMMENT '" . $sql['col_info']['comment'] . "'" : "");
						break;

					case 'varchar' :
					case 'char' :
					case 'tinytext' :
					case 'mediumtext' :
					case 'text' :
					case 'longtext' :
						$this -> cur_query .= "`" . $sql['col_info']['name'] . "` " . $sql['col_info']['type'] . ($sql['col_info']['length'] ? "(" . $sql['col_info']['length'] . ")" : "") . " collate utf8_unicode_ci" . " " . $sql['col_info']['attribs'] . ($sql['col_info']['is_null'] ? " NULL" : " NOT NULL")
						// .  ( $sql['col_info']['extra']            ? " " . $sql['col_info']['extra'] : "" )
						. ($sql['col_info']['default'] !== null ? " DEFAULT '" . $sql['col_info']['default'] . "'" : " DEFAULT NULL") . ($sql['col_info']['comment'] ? " COMMENT '" . $sql['col_info']['comment'] . "'" : "");
						break;
					case 'enum' :
					case 'set' :
						$this -> cur_query .= "`" . $sql['col_info']['name'] . "` " . $sql['col_info']['type'] . ($sql['col_info']['length'] ? "(" . $sql['col_info']['length'] . ")" : "") . " collate utf8_unicode_ci" . ($sql['col_info']['is_null'] ? " NULL" : " NOT NULL") . ($sql['col_info']['default'] !== null ? " DEFAULT '" . $sql['col_info']['default'] . "'" : " DEFAULT NULL") . ($sql['col_info']['comment'] ? " COMMENT '" . $sql['col_info']['comment'] . "'" : "");
						break;
				}

				# KEYs

				if (isset($sql['col_info']['indexes']) and is_array($sql['col_info']['indexes']) and count($sql['col_info']['indexes'])) {
					foreach ($sql['col_info']['indexes'] as $k => $v) {
						if ($v === true) {
							$this -> cur_query .= ", ADD " . $k . " (" . $sql['col_info']['name'] . ")";
						}
					}
				}
				break;

			case 'drop_column' :
				$i = 0;
				foreach ($sql['col_info'] as $col_name => $col_info) {
					$this -> cur_query .= (($i == 0) ? "DROP `" : ", DROP `") . $col_info['name'] . "` ";
					$i++;
				}
				break;

			case 'change_column' :
				$i = 0;
				foreach ($sql['col_info'] as $col_name => $col_info) {
					$this -> cur_query .= (($i == 0) ? "CHANGE `" : ", CHANGE `") . $col_info['old_name'] . "` ";
					switch ( strtolower( $col_info['type'] ) ) {
						case 'tinyint' :
						case 'smallint' :
						case 'mediumint' :
						case 'int' :
						case 'bigint' :
						case 'float' :
						case 'double' :
						case 'decimal' :
							$this -> cur_query .= "`" . $col_info['name'] . "` " . $col_info['type'] . ($col_info['length'] ? "(" . $col_info['length'] . ")" : "") . " " . $col_info['attribs'] . ($col_info['is_null'] ? " NULL" : " NOT NULL")
							// .  ( $col_info['extra']            ? " " . $col_info['extra'] : "" )
							. ($col_info['default'] !== null ? " DEFAULT '" . $col_info['default'] . "'" : " DEFAULT NULL") . ($col_info['comment'] ? " COMMENT '" . $col_info['comment'] . "'" : "");
							break;

						case 'varchar' :
						case 'char' :
						case 'tinytext' :
						case 'mediumtext' :
						case 'text' :
						case 'longtext' :
							$this -> cur_query .= "`" . $col_info['name'] . "` " . $col_info['type'] . ($col_info['length'] ? "(" . $col_info['length'] . ")" : "") . " collate utf8_unicode_ci" . " " . $col_info['attribs'] . ($col_info['is_null'] ? " NULL" : " NOT NULL")
							// .  ( $col_info['extra']            ? " " . $col_info['extra'] : "" )
							. ($col_info['default'] !== null ? " DEFAULT '" . $col_info['default'] . "'" : " DEFAULT NULL") . ($col_info['comment'] ? " COMMENT '" . $col_info['comment'] . "'" : "");
							break;
						case 'enum' :
						case 'set' :
							$this -> cur_query .= "`" . $col_info['name'] . "` " . $col_info['type'] . ($col_info['length'] ? "(" . $col_info['length'] . ")" : "") . " collate utf8_unicode_ci" . ($col_info['is_null'] ? " NULL" : " NOT NULL") . ($col_info['default'] !== null ? " DEFAULT '" . $col_info['default'] . "'" : " DEFAULT NULL") . ($col_info['comment'] ? " COMMENT '" . $col_info['comment'] . "'" : "");
							break;
					}

					# KEYs

					if (isset($col_info['indexes']) and is_array($col_info['indexes']) and count($col_info['indexes'])) {
						foreach ($col_info['indexes'] as $k => $v) {
							if ($v === true) {
								$this -> cur_query .= ", ADD " . $k . " (" . $col_info['name'] . ")";
							}
						}
					}

					$i++;
				}
				break;

			//-------------
			// "COMMENT"
			//-------------

			case 'comment' :
				$this -> cur_query .= " COMMENT '" . $sql['comment'] . "'";
				break;
		}

		try {
			$stmt = new Zend_Db_Statement_Pdo($this -> db, $this -> cur_query);
			return $stmt -> execute();
		} catch ( Zend_Db_Exception $e ) {
			$this -> Registry -> exception_handler($e);
			return false;
		}
	}

	/**
	 * Drops table(s)
	 *
	 * @param    array     List of tables to be dropped
	 * @return   mixed     # of affected rows on success, FALSE otherwise
	 */
	public function simple_exec_drop_table($tables) {
		//-----------------
		// Build and exec
		//-----------------

		if (!is_array($tables)) {
			return false;
		} else {
			if (!count($tables)) {
				return false;
			} else {
				$tables = $this -> attach_prefix($tables);
				foreach ($tables as &$table) {
					$table = $this -> db -> quoteIdentifier($table, true);
				}
				$this -> cur_query = "DROP TABLE IF EXISTS " . implode(", ", $tables);
			}
		}

		try {
			$stmt = new Zend_Db_Statement_Pdo($this -> db, $this -> cur_query);
			$this -> query_count++;
			return $stmt -> execute();
		} catch ( Zend_Db_Exception $e ) {
			$this -> Registry -> exception_handler($e);
			return false;
		}
	}

	/**
	 * Builds "CREATE TABLE ..." query from Table-Structure Array and executes it
	 *
	 * @param    array     Struct array
	 * @return   integer   # of queries executed
	 */
	public function simple_exec_create_table_struct($struct) {
		if (!count($struct)) {
			return 0;
		}

		//-----------------
		// Build and exec
		//-----------------

		$i = 0;
		$q_count = 0;
		foreach ($struct['tables'] as $table => $data) {
			//------------------------
			// DROP TABLE IF EXISTS
			//------------------------

			$this -> simple_exec_drop_table(array($table));

			//--------------------------------
			// Build CREATE TABLE statement
			//--------------------------------

			$this -> cur_query = "CREATE TABLE IF NOT EXISTS " . $this -> db -> quoteIdentifier($this -> attach_prefix($table), true) . " (\n";

			# CREATE TABLE...
			# Reset KEY data
			$key = array();

			foreach ($data['col_info'] as $_column_name => $_column_struct) {
				# Handling columns
				switch ( strtolower( $_column_struct['type'] ) ) {
					case 'tinyint' :
					case 'smallint' :
					case 'mediumint' :
					case 'int' :
					case 'bigint' :
					case 'float' :
					case 'double' :
					case 'decimal' :
						$this -> cur_query .= $this -> db -> quoteIdentifier($_column_name, true) . " " . $_column_struct['type'] . ($_column_struct['length'] ? "(" . $_column_struct['length'] . ")" : "") . " " . $_column_struct['attribs'] . ($_column_struct['is_null'] ? " NULL" : " NOT NULL") . ($_column_struct['extra'] ? " " . $_column_struct['extra'] : "") . ($_column_struct['default'] !== null ? " DEFAULT '" . $_column_struct['default'] . "'" : "") . ",\n";
						break;
					case 'varchar' :
					case 'char' :
					case 'tinytext' :
					case 'mediumtext' :
					case 'text' :
					case 'longtext' :
						$this -> cur_query .= $this -> db -> quoteIdentifier($_column_name, true) . " " . $_column_struct['type'] . ($_column_struct['length'] ? "(" . $_column_struct['length'] . ")" : "") . " collate utf8_unicode_ci" . " " . $_column_struct['attribs'] . ($_column_struct['is_null'] ? " NULL" : " NOT NULL") . ($_column_struct['extra'] ? " " . $_column_struct['extra'] : "") . ($_column_struct['default'] !== null ? " DEFAULT '" . $_column_struct['default'] . "'" : "") . ",\n";
						break;
				}

				# Handling KEYs
				if (isset($_column_struct['indexes']) and is_array($_column_struct['indexes']) and count($_column_struct['indexes'] >= 1)) {
					foreach ($_column_struct['indexes'] as $k => $v) {
						if ($v === true) {
							$key[] = $k . " KEY " . $this -> db -> quoteIdentifier($_column_name, true) . "(" . $this -> db -> quoteIdentifier($_column_name, true) . ")";
						}
						# $v is index_type, e.g. "USING {BTREE | HASH}"
						else {
							$key[] = $k . " KEY " . $v . " " . $this -> db -> quoteIdentifier($_column_name, true) . "(" . $this -> db -> quoteIdentifier($_column_name, true) . ")";
						}
					}
				}
			}

			# Attaching KEYs to the rest of query
			$this -> cur_query .= implode(",\n", $key) . "\n";

			# Finalizing CREATE TABLE ... query
			$data['storage_engine'] = (isset($data['storage_engine']) and !empty($data['storage_engine'])) ? $data['storage_engine'] : "MyISAM";
			$data['charset'] = (isset($data['charset']) and !empty($data['charset'])) ? $data['charset'] : "utf8";
			$data['collate'] = (isset($data['collate']) and !empty($data['collate'])) ? $data['collate'] : "utf8_unicode_ci";

			# Let's clean-up COMMENT a bit
			if (isset($data['comment'])) {
				$data['comment'] = html_entity_decode($data['comment'], ENT_QUOTES, "UTF-8");
				// Decoding characters
				$data['comment'] = preg_replace("/'{2,}/", "'", $data['comment']);
				// Excessive single-quotes
				if (mb_strlen($data['comment']) > 60)// Truncate here, as you might get trailing single-quotes later on, for comments with strlen() close to 60.
				{
					$data['comment'] = mb_substr($data['comment'], 0, 60);
				}
				$data['comment'] = str_replace("'", "''", $data['comment']);
				// Single quotes are doubled in number. This is MySQL syntax!
			} else {
				$data['comment'] = "";
			}

			$this -> cur_query .= ") ENGINE=" . $data['storage_engine'] . " DEFAULT CHARACTER SET=" . $data['charset'] . " COLLATE=" . $data['collate'] . " COMMENT='" . $data['comment'] . "';\n\n";

			# Execute
			$return = 0;
			try {
				$stmt = new Zend_Db_Statement_Pdo($this -> db, $this -> cur_query);
				$this -> query_count++;
				return $stmt -> execute();
			} catch ( Zend_Db_Exception $e ) {
				$this -> Registry -> exception_handler($e);
				return false;
			}
		}

		return $return;
	}

}
