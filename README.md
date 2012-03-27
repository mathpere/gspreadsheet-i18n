# GSpreadsheet-i18n

This script aims to synchronize i18n properties with Google Spreadsheet.

## Usage

```shell

groovy gspreadsheet-i18n.groovy 

 -d,--workingDirectory <Working directory>   The directory where to find/save properties files.
                                             Default is script directory

 -k,--spreadsheetKey <Spreadsheet key>       The spreadsheet key: https://docs.google.com/spreadsheet/ccc?key={KEY}

 -l,--defaultLanguage <Default language>     The default language with no extension. 
	                                         Default is "en".

 -n,--baseName <Base name>                   the base name of i18n files {baseName}_en.properties.
                                             Default is "messages".

 -s,--sync <Synchronization>                 Sync properties files <from> or <to> Google Spreadsheet.
                                             Default is from.
```

## Sample



## Bug tracker

Please report any issue on GitHub: 

https://github.com/mathpere/gspreadsheet-i18n/issues


## Authors

**Mathieu Perez**

+ http://twitter.com/mathieuperez


## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0