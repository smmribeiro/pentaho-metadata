/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.metadata.query.model.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metadata.messages.Messages;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;

public class CsvDataReader {

  private static final Log logger = LogFactory.getLog( QueryXmlHelper.class );

  private String fileLocation;
  private boolean headerPresent;
  private String enclosure;
  private String delimiter;
  private int rowLimit;

  // generated values
  private List<String> header;
  private List<List<String>> data;
  private int columnCount;

  public CsvDataReader( String fileLocation, boolean headerPresent, String delimiter, String enclosure, int rowLimit ) {
    this.fileLocation = fileLocation;
    this.headerPresent = headerPresent;
    this.delimiter = delimiter;
    this.enclosure = enclosure;
    this.rowLimit = rowLimit;
  }

  public List<List<String>> loadData() {
    String line = null;
    List<List<String>> dataSample = new ArrayList<List<String>>( rowLimit );
    List<String> rowData = null;
    InputStreamReader reader = null;
    CSVTokenizer csvt = null;
    LineIterator lineIterator = null;
    try {
      InputStream inputStream = KettleVFS.getInputStream( fileLocation );
      reader = new InputStreamReader( inputStream );
      lineIterator = new LineIterator( reader );
      int row = 0;
      int count;
      while ( row < rowLimit && lineIterator.hasNext() ) {
        line = lineIterator.nextLine();
        ++row;

        csvt = new CSVTokenizer( line, delimiter, enclosure, false );
        rowData = new ArrayList<String>();
        count = 0;

        while ( csvt.hasMoreTokens() ) {
          // get next token and store it in the list
          rowData.add( csvt.nextToken() );
          count++;
        }

        if ( columnCount < count ) {
          columnCount = count;
        }

        if ( headerPresent && row == 1 ) {
          header = rowData;
        } else {
          dataSample.add( rowData );
        }
      }
    } catch ( KettleFileException e ) {
      logger.error( Messages.getString( "CsvDataReader.ERROR_0001_Failed" ), e ); //$NON-NLS-1$
    } finally {
      LineIterator.closeQuietly( lineIterator );
      IOUtils.closeQuietly( reader );
    }
    data = dataSample;
    return dataSample;
  }

  public List<String> getHeader() {
    return header;
  }

  public List<String> getColumnData( int columnNumber ) {
    List<String> dataSample = new ArrayList<String>( rowLimit );
    for ( List<String> row : data ) {
      if ( row.size() > columnNumber ) {
        dataSample.add( row.get( columnNumber ) );
      }
    }
    return dataSample;
  }

  public int getColumnCount() {
    return columnCount;
  }

}
