package de.elbosso.tools;

import de.elbosso.algorithms.graph.Graph;
import de.elbosso.algorithms.graph.Vertex;
import de.elbosso.util.Stringifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.netpreserve.jwarc.MediaType;
import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcRecord;
import org.netpreserve.jwarc.WarcResponse;
import us.codecraft.xsoup.XElements;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Arrays;

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
	private static java.util.List<java.util.regex.Pattern> pathBlacklist= Arrays.asList(new java.util.regex.Pattern[]
			{
					java.util.regex.Pattern.compile("/tag_.*")
					,java.util.regex.Pattern.compile("/index.*")
					,java.util.regex.Pattern.compile("/resources/.*")
					,java.util.regex.Pattern.compile("/images/.*")
					,java.util.regex.Pattern.compile("/month.*")
					,java.util.regex.Pattern.compile("/week.*")
			});
	private static java.util.List<java.util.regex.Pattern> pathWhitelist= Arrays.asList(new java.util.regex.Pattern[]
			{
					java.util.regex.Pattern.compile(".*\\.html")
			});
	private static java.util.List<java.lang.String> xpathsToSearch= Arrays.asList(new java.lang.String[]
			{
					"//div[@id='abstract']"
					,"//div[@id='contentdiv']"
			});

	public static void main(java.lang.String [] args) throws IOException
	{
		java.io.PrintWriter pw=new java.io.PrintWriter("/tmp/map.gv");
		java.util.Map<String, Vertex<String,Object>> nodes=new java.util.HashMap();
		java.util.Map<String,java.util.List<Vertex<String,Object>>> connections=new java.util.HashMap();
		try (WarcReader reader = new WarcReader(FileChannel.open(Paths.get("/home/elbosso/elbosso.warc")))) {
			for (WarcRecord record : reader) {
				if (record instanceof WarcResponse && record.contentType().base().equals(MediaType.HTTP)) {
					WarcResponse response = (WarcResponse) record;
					if(response.http().contentType().base().equals(MediaType.HTML))
					{
						boolean include=pathWhitelist.isEmpty();
						if(include==false)
						{
							for (java.util.regex.Pattern pattern : pathWhitelist)
							{
								if (pattern.matcher(new java.net.URL(response.target()).getPath()).matches())
								{
									include = true;
									break;
								}
							}
						}
						if(include==true)
						{
							boolean exclude = false;
							for (java.util.regex.Pattern pattern : pathBlacklist)
							{
								if (pattern.matcher(new java.net.URL(response.target()).getPath()).matches())
								{
									exclude = true;
									break;
								}
							}
							if (exclude == false)
							{
								java.net.URL url=new java.net.URL(response.target());
								if(url.getPath().startsWith("/$")==false)
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
									if(nodes.containsKey(url.getPath().substring(1))==false)
									{
										Vertex<java.lang.String, Object> node = new Vertex(url.getPath().substring(1));
										nodes.put(node.getUserData(),node);
									}
									Document document = Jsoup.parse(response.body().stream(), null, url.getProtocol() + "://" + url.getHost() + ":" + url.getPort());

									java.util.Set<String> elements = new java.util.HashSet();
									for (java.lang.String xpathToSearch : xpathsToSearch)
									{
										elements.addAll(new java.util.HashSet(Xsoup.compile(xpathToSearch + "//a/@href").evaluate(document).list()));
									}
									java.util.List<Vertex<String,Object>> localConnections=null;
									for (java.lang.String element : elements)
									{
										if (element.trim().length() > 0)
										{
											if ((element.toUpperCase().startsWith("HTTP://") == false) &&
													(element.toUpperCase().startsWith("HTTPS://") == false) &&
													(element.toUpperCase().startsWith("$") == false)
											)
											{
												System.out.println(element);
/*												pw.print(massage(url.getPath().substring(1)));
												pw.print(" -> ");
												pw.println(massage(element));
*/	 	 										if(nodes.containsKey(element)==false)
											{
												Vertex<java.lang.String, Object> node = new Vertex(element);
												nodes.put(node.getUserData(),node);
											}
												if(localConnections==null)
												{
													if(connections.containsKey(url.getPath().substring(1))==false)
													{
														connections.put(url.getPath().substring(1),new java.util.LinkedList());
													}
													localConnections=connections.get(url.getPath().substring(1));
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
		for(Vertex<String, Object> vertex:graph)
		{
			vertex.getAttributes().put(Graph.DOT_STYLE,"margin="+(0.01*vertex.getConnections().size())+",fontsize=\""+(14+(int)(2*vertex.getConnections().size()))+"pt\",penwidth="+(1.0+0.1*vertex.getConnections().size()));
		}
		pw.println(graph.toDotString(new Stringifier<Vertex<String, Object>>()
		{
			@Override
			public String toString(Vertex<String, Object> client)
			{
				return client.getUserData();
			}
		}));
		pw.close();
	}
}