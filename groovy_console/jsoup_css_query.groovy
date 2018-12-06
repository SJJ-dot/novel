import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

def parse = Jsoup.parse(new URL("http://www.yunlaige.com/book/19984.html"),10000)
def select = parse.select(".book-info > *")
for (Element element : select) {
    println(element)
}

def select2 = parse.select(".book-info *")

for (Element element : select2) {
    println(element)
}