import org.jsoup.Jsoup
import org.jsoup.nodes.Document

def parse = Jsoup.parse("http://www.open-open.com/jsoup/selector-syntax.htm")
println parse