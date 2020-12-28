package dev.squaremile.transport.usecases.market.application;

public class ChartTemplate
{
    static String chartRendering()
    {
        return "<html>\n" +
               "<head>\n" +
               "<script type=\"text/javascript\"\n" +
               "  src=\"dygraph.js\"></script>\n" +
               "<link rel=\"stylesheet\" src=\"dygraph.css\" />\n" +
               "</head>\n" +
               "<body>\n" +
               "<div id=\"graphdiv\" style=\"width:100%\"></div>\n" +
               "<script type=\"text/javascript\">\n" +
               "  g = new Dygraph(\n" +
               "  document.getElementById(\"graphdiv\"),\n" +
               "   \"/data.txt\", \n" +
               "  {\n" +
               "  legend: 'always',\n" +
               "  title: '',\n" +
               "  showRoller: true,\n" +
               "  customBars: true,\n" +
               "  ylabel: 'price',\n" +
               "}\n" +
               ");\n" +
               "</script>\n" +
               "</body>\n" +
               "</html>";
    }
}
