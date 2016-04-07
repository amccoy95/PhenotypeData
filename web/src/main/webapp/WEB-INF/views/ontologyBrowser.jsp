<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:genericpage>

    <jsp:attribute name="title">IMPC Search</jsp:attribute>
  <jsp:attribute name="breadcrumb">&nbsp;&raquo;&nbsp;<a href="${baseUrl}/search/${dataType}?kw=*">${dataTypeLabel}</a> &raquo; ${searchQuery}</jsp:attribute>
  <jsp:attribute name="bodyTag"><body id="top" class="page-node searchpage one-sidebar sidebar-first small-header"></jsp:attribute>

	<jsp:attribute name="header">       
        <link type="text/css" rel="stylesheet" href="${baseUrl}/css/ontologyBrowser.css"/>
        <link type="text/css" rel="stylesheet" href="${baseUrl}/css/vendor/jstree.min.css"/>
	</jsp:attribute>

	<jsp:attribute name="addToFooter">	
		<div class="region region-pinned"></div>
	</jsp:attribute>

  	<jsp:body>

 	 <script type="text/javascript" src='${baseUrl}/js/vendor/jstree/jstree.min.js'></script>

  
  	 <h1 id="h1tree">Browse Mammalian Phenotype Ontology</h1>
  	 <div id="tree"></div>

	 <script  type="text/javascript" >

		 var termId = "${termId}";
		 var clickToOpen = false; // The tree expands too many nodes at load time. This is a hack to only allow it on mouseclick
		 
		 var ontologyTree = $('#tree').jstree({
			  "core" : {
				    "animation" : 0,
				    'data' : {	
				    	'url' : function (node) {
				    	      return node.id === '#' ?
				    	    		"ontologyBrowser2?termId=" + termId + "&node=src" :
				    	    		"ontologyBrowser2?termId=" + termId + "&node=" + node.id;
				    	    },
				      	'data' : function (node) {
				        	return { 'id' : node.id };
				      	}
			  		}
			  },
			  "types" : {
				     "default" : {
				       "icon" : "img/jstree/jstree-node.png"
				     },
				     "selected" : {
				       "icon" : "img/jstree/jstree-node-selected.png"
				     }
			},
			  "plugins" : [
			  	"types"
			  ]
		});
		
	
		$("#tree").delegate("a","click", function(e) {
			window.location.href = "../phenotypes/"  + data.node.term_id;
		});
		
	 </script>


  	</jsp:body>
</t:genericpage>

