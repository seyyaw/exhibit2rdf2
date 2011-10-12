 <html>
 <head>
    <title>Rivela Faceted Visualization</title>
  
  
    <link href="rivela.js" type="application/json" rel="exhibit/data" />
  
    <script src="http://static.simile.mit.edu/exhibit/api-2.0/exhibit-api.js"
          	  type="text/javascript"></script>
             <script src="http://static.simile.mit.edu/exhibit/extensions-2.0/time/time-extension.js"
   			  type="text/javascript"></script>
            
    <style>
   </style>

 </head> 
 <body>
    <h1>Rivela Faceted Visualization</h1>
    <table width="100%">
        <tr valign="top">
         <td ex:role="viewPanel">
                <div ex:role="view"></div>
            </td>
        
         	<!-- 
            <td ex:role="viewPanel">
            	 <div ex:role="view"
				     ex:viewClass="Timeline"
				     ex:start=".birthdate"
				     ex:colorKey=".gender">
				 </div>
			<div ex:role="exhibit-view"
			     ex:viewClass="Exhibit.TabularView"
			     ex:columns=".label, .birthdate, .gender, .firstName, .lastname"
			     ex:columnLabels="Full name, Birth Date, Gender, First Name, Last Name"
			     ex:columnFormats="list, list, list, list, list"
			     ex:sortColumn="3"
			     ex:sortAscending="false">
			 </div>
			 </td>
			 -->	
			 		
            
            <td width="25%">
            	 <div>Search Rivela content:</div>
            	 <div  ex:role="facet" ex:facetClass="TextSearch"></div>
             	 <div ex:role="facet" ex:expression=".type" ex:facetLabel="Type"></div>
            </td>
        </tr>
    </table>
 </body>
 </html>

