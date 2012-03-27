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

https://docs.google.com/spreadsheet/ccc?key=ABCDEFG1234567

```shell

# list working directory content:
$ ls -l ~/Sources/myApp/grails-app/i18n/
messages_cs_CZ.properties
messages_da.properties
messages_de.properties
messages_en.properties
messages_es.properties
messages_fr.properties
messages_it.properties
messages_ja.properties
messages_nl.properties
messages_pt_BR.properties
messages_pt_PT.properties
messages_ru.properties
messages_sv.properties
messages_th.properties
messages_zh_CN.properties


# synchronize TO spreadsheet:
groovy gspreadsheet-i18n.groovy -k ABCDEFG1234567 -s to -d ~/Sources/myApp/grails-app/i18n/


# synchronize FROM spreadsheet:
groovy gspreadsheet-i18n.groovy -k ABCDEFG1234567 -s from -d ~/Sources/myApp/grails-app/i18n/


# force property file base name:
groovy gspreadsheet-i18n.groovy -k ABCDEFG1234567 -s from -d ~/Sources/myApp/grails-app/i18n/ -n messages



```

## Bug tracker

Please report any issue on GitHub: 

https://github.com/mathpere/gspreadsheet-i18n/issues


## Authors

**Mathieu Perez**

+ http://twitter.com/mathieuperez


## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0