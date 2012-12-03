//http://burtsev.net/en/2012/01/29/149
@GrabResolver(name = 'eburtsev', root = 'https://raw.github.com/eburtsev/gdata-maven/master/')
@Grapes([
@Grab(group = 'com.google.gdata.gdata-java-client', module = 'gdata-client-1.0', version = '1.46.0'),
@Grab(group = 'com.google.gdata.gdata-java-client', module = 'gdata-core-1.0', version = '1.46.0'),
@Grab(group = 'com.google.gdata.gdata-java-client', module = 'gdata-docs-3.0', version = '1.46.0'),
@Grab(group = 'com.google.gdata.gdata-java-client', module = 'gdata-spreadsheet-3.0', version = '1.46.0'),
@Grab(group = 'com.google.gdata.gdata-java-client', module = 'gdata-media-1.0', version = '1.46.0'),
@Grab(group = 'javax.mail', module = 'mail', version = '1.4.4')
])
import com.google.gdata.data.Link
import com.google.gdata.data.batch.BatchOperationType
import com.google.gdata.data.batch.BatchStatus
import com.google.gdata.data.batch.BatchUtils
import com.google.gdata.client.DocumentQuery
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer
import com.google.gdata.client.docs.DocsService
import com.google.gdata.data.DateTime
import com.google.gdata.data.MediaContent
import com.google.gdata.data.PlainTextConstruct
import com.google.gdata.data.docs.DocumentListEntry
import com.google.gdata.data.docs.DocumentListEntry.MediaType
import com.google.gdata.data.docs.DocumentListFeed
import com.google.gdata.data.docs.FolderEntry
import com.google.gdata.data.extensions.When
import com.google.gdata.data.extensions.Where
import com.google.gdata.data.media.MediaFileSource
import com.google.gdata.util.ResourceNotFoundException
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.client.spreadsheet.*
import com.google.gdata.data.spreadsheet.*
import com.google.gdata.util.*
import java.awt.Desktop

public class SortedProperties extends Properties {
  
  @SuppressWarnings("unchecked")
  public synchronized Enumeration keys() {
     Enumeration keysEnum = super.keys();
     Vector keyList = new Vector();
     while(keysEnum.hasMoreElements()){
       keyList.add(keysEnum.nextElement());
     }
     Collections.sort(keyList);
     return keyList.elements();
  }
  
}

// --------------------------
// script utils
// --------------------------
def oauthHelper
def oauthParameters

def endsWith = { code, msg ->

    if ( oauthHelper && oauthParameters ) {
        println "Revoking OAuth Token..."
        oauthHelper.revokeToken(oauthParameters)
        println "OAuth Token revoked."
    }

    println "end of script with code: ${code} and msg: ${msg}"
    System.exit(code)
}

def cliBuilder = new CliBuilder(usage: "groovy gspreadsheet-i18n.groovy")

// available options
cliBuilder.k(longOpt: 'spreadsheetKey', args: 1, argName: 'Spreadsheet key', required: true, 'The spreadsheet key: https://docs.google.com/spreadsheet/ccc?key={KEY}')
cliBuilder.d(longOpt: 'workingDirectory', args: 1, argName: 'Working directory', required: false, 'The directory where to find/save properties files. Default is script directory')
cliBuilder.l(longOpt: 'defaultLanguage', args: 1, argName: 'Default language', required: false, 'The default language with no extension. Default is "en".')
cliBuilder.n(longOpt: 'baseName', args: 1, argName: 'Base name', required: false, 'the base name of i18n files {baseName}_en.properties. Default is "messages".')
cliBuilder.s(longOpt: 'sync', args: 1, argName: 'Synchronization', required: false, 'Sync properties files <from> or <to> Google Spreadsheet. Default is from.')

def options = cliBuilder.parse(args)

if ( !options ) {
    endsWith(1, "Missing argument(s), see usage")
}

def spreadsheetKey = options.k
def workingDirectory = options.d ? new File(options.d) : new File(getClass().protectionDomain.codeSource.location.path).parentFile
def defaultLanguage = options.l ?: "en"
def baseName = options.n ?: "messages"
def sync = options.s ?: "from"

def filenamePattern = "${baseName}.properties|${baseName}_([a-zA-Z_]+).properties"

println ""
println " - Spreadsheet key:  ${spreadsheetKey} "
println " - Working directory:  ${workingDirectory} "
println " - Default language:  ${defaultLanguage} "
println " - Base name:  ${baseName} "
println " - Synchronization:  ${sync.toUpperCase()}  Google spreadsheet "
println ""

////////////////////////////////////////////////////////////////////////////
// Grant Access
////////////////////////////////////////////////////////////////////////////

println "Grant access..."

oauthParameters = new GoogleOAuthParameters()
oauthParameters.OAuthConsumerKey = "anonymous"
oauthParameters.OAuthConsumerSecret = "anonymous"
oauthParameters.scope = "https://spreadsheets.google.com/feeds https://docs.google.com/feeds/"

def signer = new OAuthHmacSha1Signer()
oauthHelper = new GoogleOAuthHelper(signer)
oauthHelper.getUnauthorizedRequestToken(oauthParameters)
String userAuthorizationUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters)

println userAuthorizationUrl

println "Please visit the URL to authorize your OAuth request token. Once that is complete, press any key to continue..."

Desktop.getDesktop().browse(new URI(userAuthorizationUrl))

System.in.read()

String token = oauthHelper.getAccessToken(oauthParameters)
println " OAuth Access Token:  ${token} "

////////////////////////////////////////////////////////////////////////////
// Perform operation
////////////////////////////////////////////////////////////////////////////

SpreadsheetService service = new SpreadsheetService("gspread-i18n-service")
service.setOAuthCredentials(oauthParameters, signer)

println "Retrieve worksheets..."

def worksheetsUrl = new URL("https://spreadsheets.google.com/feeds/worksheets/${spreadsheetKey}/private/full")

def feed = service.getFeed(worksheetsUrl, WorksheetFeed.class)

def worksheets = feed.entries

if ( !worksheets ) {
    endsWith(1, "No spreadsheet found with key=${spreadsheetKey}...")
}

switch ( sync ) {

    case "to":

        def numberOfWorksheetsToDelete = worksheets.size()

        workingDirectory.listFiles({ File f, String filename ->

            (filename =~ filenamePattern).matches()

        } as FilenameFilter).each { file ->

            def language = (file.name =~ filenamePattern)[0][1] ?: defaultLanguage

            println "Create worksheet '${language}'..."

            def props = new Properties()
            props.load(new InputStreamReader(new FileInputStream(file), "UTF-8"))

            def worksheet = new WorksheetEntry()
            worksheet.title = new PlainTextConstruct(language)
            worksheet.colCount = 2
            worksheet.rowCount = props.size()
            worksheet = service.insert(worksheetsUrl, worksheet)

            CellFeed cellFeed = service.getFeed(new URI(worksheet.cellFeedUrl.toString()
                    + "?min-col=1&max-col=2").toURL(), CellFeed.class)


            def batchRequest = new CellFeed()

            props.sort { it.key }.eachWithIndex { prop, idx ->

                def row = idx + 1

                def batchEntry = new CellEntry(row, 1, prop.key)
                batchEntry.setId(worksheet.cellFeedUrl.toString() + "/" + String.format("R%sC%s", row, 1))
                BatchUtils.setBatchId(batchEntry, String.format("R%sC%s", row, 1))
                BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE)
                batchRequest.getEntries().add(batchEntry)

                batchEntry = new CellEntry(row, 2, prop.value)
                batchEntry.setId(worksheet.cellFeedUrl.toString() + "/" + String.format("R%sC%s", row, 2))
                BatchUtils.setBatchId(batchEntry, String.format("R%sC%s", row, 2))
                BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE)
                batchRequest.getEntries().add(batchEntry)

            }

            // Submit the update
            Link batchLink = cellFeed.getLink(com.google.gdata.data.ILink.Rel.FEED_BATCH, com.google.gdata.data.ILink.Type.ATOM)

            service.setHeader("If-Match", "*")
            CellFeed batchResponse = service.batch(new URL(batchLink.getHref()), batchRequest)
            service.setHeader("If-Match", null)

            // Check the batch status
            batchResponse.entries.each { entry ->
	
                def batchId = BatchUtils.getBatchId(entry)

                if (!BatchUtils.isSuccess(entry)) {
                   BatchStatus status = BatchUtils.getBatchStatus(entry)
                   println "${batchId} failed (${status.reason}) ${status.content}"
                }
            }
        }

        println "Delete old worksheets..."

        service.getFeed(worksheetsUrl, WorksheetFeed.class).entries.subList(0, numberOfWorksheetsToDelete)*.delete()

        break

    case "from":
    default:

        worksheets.each { worksheet ->

            def worksheetTitle = worksheet.title.plainText

            def props = new SortedProperties()

            println "Retrieve cell feed from '${worksheetTitle}'..."

            def cellFeed = service.getFeed(new URI(worksheet.cellFeedUrl.toString()
                    + "?min-col=1&max-col=2").toURL(), CellFeed.class)

            cellFeed.entries*.cell.groupBy {it.row}.each { row, cells ->
                props.setProperty(cells.find {it.col == 1}.inputValue, cells.find {it.col == 2}.inputValue)
            }

            def outputs = [new File(workingDirectory, "${baseName}_${worksheetTitle}.properties")]

            if ( worksheetTitle == defaultLanguage ) {
                outputs << new File(workingDirectory, "${baseName}.properties")
            }

            outputs.each { output ->
                props.store(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), null)
            }

        }
        break
}

endsWith(0, "Good bye!")
