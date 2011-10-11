package it.okkam.exhibit;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Rdf2Json
 */
public class Rdf2Json extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Map<String, String> loginInfo = new HashMap<String, String>();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Rdf2Json() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			String userName=request.getParameter("userName");
			String password=request.getParameter("pwd");
			Map logininfo=authenticate();
			//dopo il login di successo
			if (logininfo.containsKey(userName) && logininfo.containsValue(password)) {
				/*//creazione di modello di dati
				DataModel dataModel=new DataModel();
				//percorso del file di output dove json salvato
				String filepath=request.getServletContext().getRealPath("rivela.js");
				//ricevendo il contextGraph rilevanti
				String contextGraph=dataModel.getContextGraph(userName);
				dataModel.creatJSON(filepath, contextGraph);
				//visualizzazione dei risultati
				
				request.getRequestDispatcher("rivela.jsp").forward(request, response);*/
				
				request.getRequestDispatcher("search.jsp").forward(request, response);
			}	
		}
	public Map authenticate() {
		ServletContext sc = getServletContext();
		try {
			String filepath = sc.getRealPath("WEB-INF/resources/userNames.txt");
			System.out.println("path of authentication file=" + filepath);
			FileInputStream fstream = new FileInputStream(filepath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				loginInfo.put(
						strLine.substring(0, strLine.indexOf(",")),
						strLine.substring(strLine.indexOf(",") + 1,
								strLine.length()));
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error message: " + e.getMessage());
		}
		return loginInfo;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
