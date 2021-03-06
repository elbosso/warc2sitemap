package de.elbosso.tools;

import de.elbosso.algorithms.graph.Edge;
import de.elbosso.algorithms.graph.Graph;
import de.elbosso.algorithms.graph.Vertex;
import de.elbosso.util.Stringifier;
import de.elbosso.util.generator.generalpurpose.RandomColor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.netpreserve.jwarc.MediaType;
import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcRecord;
import org.netpreserve.jwarc.WarcResponse;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;

/*
Copyright (c) 2012-2020.

Juergen Key. Alle Rechte vorbehalten.

Weiterverbreitung und Verwendung in nichtkompilierter oder kompilierter Form,
mit oder ohne Veraenderung, sind unter den folgenden Bedingungen zulaessig:

   1. Weiterverbreitete nichtkompilierte Exemplare muessen das obige Copyright,
die Liste der Bedingungen und den folgenden Haftungsausschluss im Quelltext
enthalten.
   2. Weiterverbreitete kompilierte Exemplare muessen das obige Copyright,
die Liste der Bedingungen und den folgenden Haftungsausschluss in der
Dokumentation und/oder anderen Materialien, die mit dem Exemplar verbreitet
werden, enthalten.
   3. Weder der Name des Autors noch die Namen der Beitragsleistenden
duerfen zum Kennzeichnen oder Bewerben von Produkten, die von dieser Software
abgeleitet wurden, ohne spezielle vorherige schriftliche Genehmigung verwendet
werden.

DIESE SOFTWARE WIRD VOM AUTOR UND DEN BEITRAGSLEISTENDEN OHNE
JEGLICHE SPEZIELLE ODER IMPLIZIERTE GARANTIEN ZUR VERFUEGUNG GESTELLT, DIE
UNTER ANDEREM EINSCHLIESSEN: DIE IMPLIZIERTE GARANTIE DER VERWENDBARKEIT DER
SOFTWARE FUER EINEN BESTIMMTEN ZWECK. AUF KEINEN FALL IST DER AUTOR
ODER DIE BEITRAGSLEISTENDEN FUER IRGENDWELCHE DIREKTEN, INDIREKTEN,
ZUFAELLIGEN, SPEZIELLEN, BEISPIELHAFTEN ODER FOLGENDEN SCHAEDEN (UNTER ANDEREM
VERSCHAFFEN VON ERSATZGUETERN ODER -DIENSTLEISTUNGEN; EINSCHRAENKUNG DER
NUTZUNGSFAEHIGKEIT; VERLUST VON NUTZUNGSFAEHIGKEIT; DATEN; PROFIT ODER
GESCHAEFTSUNTERBRECHUNG), WIE AUCH IMMER VERURSACHT UND UNTER WELCHER
VERPFLICHTUNG AUCH IMMER, OB IN VERTRAG, STRIKTER VERPFLICHTUNG ODER
UNERLAUBTE HANDLUNG (INKLUSIVE FAHRLAESSIGKEIT) VERANTWORTLICH, AUF WELCHEM
WEG SIE AUCH IMMER DURCH DIE BENUTZUNG DIESER SOFTWARE ENTSTANDEN SIND, SOGAR,
WENN SIE AUF DIE MOEGLICHKEIT EINES SOLCHEN SCHADENS HINGEWIESEN WORDEN SIND.
 */
public class Graphviz extends java.lang.Object
{
	private static java.util.List<java.util.regex.Pattern> pathBlacklist;
	private static java.util.List<java.util.regex.Pattern> pathWhitelist;
	private static java.util.List<java.util.regex.Pattern> pathEmphasizelist;
	private static de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter wordBoundaryCharacters=new de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter("-c",null,null);
	private static de.elbosso.util.lang.cmdline.NumberValuedCmdLineParameter ratio=new de.elbosso.util.lang.cmdline.NumberValuedCmdLineParameter("-r",null,null);

	public static void main(java.lang.String [] args) throws Exception
	{
		de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter pathWhitelistname=new de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter("-w",null,null);//"examples/whitelist.txt";
		de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter pathBlacklistname=new de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter("-b",null,null);//"examples/blacklist.txt";
		de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter pathEmphasizelistname=new de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter("-e",null,null);//"examples/blacklist.txt";
		de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter xpathsToSearchname=new de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter("-x",null,null);//"examples/xpathsToSearch.txt";
		de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter inputFileName=new de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter("-i",null,null);
		de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter outputFileName=new de.elbosso.util.lang.cmdline.StringValuedCmdLineParameter("-o",null,null);
		de.elbosso.util.lang.cmdline.CmdLineParameter[] cmdLineParameters=new de.elbosso.util.lang.cmdline.CmdLineParameter[]{
				pathWhitelistname
				,pathBlacklistname
				,pathEmphasizelistname
				,xpathsToSearchname
				,inputFileName
				,outputFileName
				,wordBoundaryCharacters
				,ratio
		};
		de.elbosso.util.lang.cmdline.CmdLineParameter.parse(cmdLineParameters,args);

		pathWhitelist=new java.util.LinkedList();
		if(pathWhitelistname!=null)
		{
			java.io.FileInputStream fis = new java.io.FileInputStream(pathWhitelistname.getValue());
			java.lang.String[] pathWhitelistPatterns = de.elbosso.util.Utilities.readIntoStringArray(fis);
			fis.close();
			for (java.lang.String p : pathWhitelistPatterns)
				pathWhitelist.add(java.util.regex.Pattern.compile(p));
		}
		pathBlacklist=new java.util.LinkedList();
		if(pathBlacklistname!=null)
		{
			java.io.FileInputStream fis = new java.io.FileInputStream(pathBlacklistname.getValue());
			java.lang.String[] pathBlacklistPatterns = de.elbosso.util.Utilities.readIntoStringArray(fis);
			fis.close();
			for (java.lang.String p : pathBlacklistPatterns)
				pathBlacklist.add(java.util.regex.Pattern.compile(p));
		}
		pathEmphasizelist = new java.util.LinkedList();
		if(pathEmphasizelistname!=null)
		{
			java.io.FileInputStream fis = new java.io.FileInputStream(pathEmphasizelistname.getValue());
			java.lang.String[] pathEmphasizelistPatterns = de.elbosso.util.Utilities.readIntoStringArray(fis);
			fis.close();
			for (java.lang.String p : pathEmphasizelistPatterns)
				pathEmphasizelist.add(java.util.regex.Pattern.compile(p));
		}
		java.util.List<java.lang.String> xpathsToSearch= Collections.emptyList();
		if(xpathsToSearchname!=null)
		{
			java.io.FileInputStream fis = new java.io.FileInputStream(xpathsToSearchname.getValue());
			xpathsToSearch = Arrays.asList(de.elbosso.util.Utilities.readIntoStringArray(fis));
			fis.close();
		}
		java.util.Map<String, Vertex<String,Object>> nodes=new java.util.HashMap();
		java.util.Map<String,java.util.List<Vertex<String,Object>>> connections=new java.util.HashMap();
		try (WarcReader reader = new WarcReader(FileChannel.open(Paths.get(inputFileName.getValue())))) {
			for (WarcRecord record : reader) {
				if (record instanceof WarcResponse && record.contentType().base().equals(MediaType.HTTP)) {
					WarcResponse response = (WarcResponse) record;
					if(response.http().contentType().base().equals(MediaType.HTML))
					{
						java.net.URL url=new java.net.URL(response.target());
//						if(url.getPath().trim().equalsIgnoreCase("/"))
						{
								if (includePath(url.getPath().substring(1)))
								{
										System.out.println(response.http().status() + " " +
												response.target() + " " +
												response.http().contentType() + " " +
												response.http().contentType().base() + " " +
												MediaType.HTTP
												+ " " + url.getPath()
										);
/*									pw.print(massage(url.getPath().substring(1)));
									pw.print(" [label=\"");
									pw.print(url.getPath().substring(1));
									pw.println("\"]");
*/
										if (nodes.containsKey(url.getPath().substring(1)) == false)
										{
											Vertex<java.lang.String, Object> node = new Vertex(url.getPath().substring(1));
											nodes.put(node.getUserData(), node);
										}
										Document document = Jsoup.parse(response.body().stream(), null, url.getProtocol() + "://" + url.getHost() + ":" + url.getPort());

										java.util.Set<String> elements = new java.util.HashSet();
										for (java.lang.String xpathToSearch : xpathsToSearch)
										{
											elements.addAll(new java.util.HashSet(Xsoup.compile(xpathToSearch + "//a/@href").evaluate(document).list()));
										}
										java.util.List<Vertex<String, Object>> localConnections = null;
										for (java.lang.String element : elements)
										{
											if (element.trim().length() > 0)
											{
												if ((element.startsWith(url.getProtocol() + "://" + url.getHost()))||((element.toUpperCase().startsWith("HTTP://") == false) &&
														(element.toUpperCase().startsWith("HTTPS://") == false) &&
														(element.toUpperCase().startsWith("$") == false))
												)
												{
													java.lang.String prefix=url.getProtocol() + "://" + url.getHost()+":"+url.getPort()+"/";
													if(element.startsWith(prefix))
														element=element.substring(prefix.length());
													prefix=url.getProtocol() + "://" + url.getHost()+"/";
													if(element.startsWith(prefix))
														element=element.substring(prefix.length());
													if(includePath(element))
													{
														//													System.out.println(element);
	/*												pw.print(massage(url.getPath().substring(1)));
													pw.print(" -> ");
													pw.println(massage(element));
	*/
														if (nodes.containsKey(element) == false)
														{
															Vertex<java.lang.String, Object> node = new Vertex(element);
															nodes.put(node.getUserData(), node);
														}
														if (localConnections == null)
														{
															if (connections.containsKey(url.getPath().substring(1)) == false)
															{
																connections.put(url.getPath().substring(1), new java.util.LinkedList());
															}
															localConnections = connections.get(url.getPath().substring(1));
														}
														localConnections.add(nodes.get(element));
													}
												}
											}
										}
								}
						}
					}
				}
			}
		}
		Graph<String,Object> graph=new Graph(nodes.values());
		for(String key: connections.keySet())
		{
			Vertex<String,Object> node=nodes.get(key);
			if(connections.containsKey(key))
			{
				java.util.List<Vertex<String, Object>> targets = connections.get(key);
				for (Vertex<String,Object> target : targets)
				{
					graph.connectTo(node, target);
				}
			}
		}
		java.util.Map<java.util.regex.Pattern,java.awt.Color> colors=new java.util.HashMap();
		RandomColor randomColor=new RandomColor(System.currentTimeMillis());
		for(Vertex<String, Object> vertex:graph)
		{
			java.awt.Color color=null;
			for(java.util.regex.Pattern pattern:pathEmphasizelist)
			{
				if (pattern.matcher(vertex.getUserData()).matches())
				{
					if(colors.containsKey(pattern)==false)
						colors.put(pattern,randomColor.next());
					color=colors.get(pattern);
					break;
				}
			}
			vertex.getAttributes().put(Graph.DOT_STYLE,"margin="+(0.01*vertex.getConnections().size())+",fontsize=\""+(14+(int)(2*vertex.getConnections().size()))+"pt\",penwidth="+(1.0+0.1*vertex.getConnections().size())+(color!=null?",style=filled,color=\"#"+String.format("%06X",color.getRGB()&0x00FFFFFF)+"\"":""));
			if(color!=null)
			{
				for(Edge<String, Object> edge:vertex.getConnections())
				{
					edge.getAttributes().put(Graph.DOT_STYLE,"style=dotted,penwidth=10,color=\"#"+String.format("%06X",color.getRGB()&0x00FFFFFF)+"\"");
				}
			}
		}
		java.io.PrintWriter pw=new java.io.PrintWriter(outputFileName.getValue());
		pw.println(graph.toDotString(new Stringifier<Vertex<String, Object>>()
		{
			@Override
			public String toString(Vertex<String, Object> client)
			{
				//java.lang.String l=transform(client.getUserData().replace("&","%26"),0.1,"<br/>");
				java.lang.String l=client.getUserData();
				if((ratio.getValue().doubleValue()>0)&&(wordBoundaryCharacters.getValue()!=null))
					l=de.elbosso.util.Utilities.insertLinebreaksForCompactness(l.replace("&","%26"),ratio.getValue().doubleValue(),wordBoundaryCharacters.getValue(),"\\n");
				return l.length()>0?("\""+l+"\""):"\"\"";
			}
		}));
		pw.close();
	}
	private static boolean includePath(java.lang.String path)
	{
		boolean rv = false;
		boolean include = pathWhitelist.isEmpty();
		if (include == false)
		{
			for (java.util.regex.Pattern pattern : pathWhitelist)
			{
				if (pattern.matcher(path).matches())
				{
					include = true;
					break;
				}
			}
		}
		if (include == true)
		{
			boolean exclude = false;
			for (java.util.regex.Pattern pattern : pathBlacklist)
			{
				if (pattern.matcher(path).matches())
				{
					exclude = true;
					break;
				}
			}
			if (exclude == false)
			{
				rv = true;
			}
		}
		return rv;
	}
}