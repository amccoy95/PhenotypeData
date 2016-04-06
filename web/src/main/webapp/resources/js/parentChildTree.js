        var width = 222,
        height = 200;
        
	    var cluster = d3.layout.cluster()
	        .size([height, width - 160]);
	
	    var diagonal = d3.svg.diagonal()
	        .projection(function(d) { return [d.y, d.x]; });
	

	    var orientation_left = { // right to left
	        size: [height, width],
	        x: function(d) { return width/2 - d.y * 100 / width; },
	        y: function(d) { return d.x; }
	    };
	    
	    var orientation_right = { // left to right
	   	    size: [height, width],
	   	    x: function(d) { return d.y * 100 / width; },
	 	    y: function(d) { return d.x; }
	    };

	    d3.select("#parentChild").style("width", width*2 + "px");
	    d3.select("#childDiv").style("width", width + "px");
	    d3.select("#parentDiv").style("width", width + "px");
	
	    
	    var svgP;

	    if (hasChildren && hasParents){
		    d3.select("#childDiv").style("width", width + "px");
		    d3.select("#parentDiv").style("width", width + "px");
	    } else if (hasChildren && !hasParents){
		    d3.select("#childDiv").style("width", width*2 + "px");
    		d3.selectAll("#childDiv").classed("half", false);
	    } else if (!hasChildren && hasParents){
		    d3.select("#parentDiv").style("width", width*2 + "px");
    		d3.selectAll("#parentDiv").classed("half", false);
	    }
	    
	    
	    function shortenLabel(label){	    	
	    	if (label.length > 33){
	    		return label.substring(0,27) + "...";
	    	} else {
	    		return  label;
	    	}
	    }

	    if (hasParents){
		    d3.json("../mpTree/json/" + mp_id + "?type=parents", function(error, root) {
		    	
		    	svgP = d3.select("#parentDiv").append("svg")
			        .attr("width", width)
			        .attr("height", height)
			        .append("g")
			        .attr("transform", "translate(120,0)");
		    	 
			     var diagonalP = d3.svg.diagonal()
		        	.projection(function(d) { return [d.y, d.x]; });
		    	
			     if (error){ 
			    	  console.log(error);
			    	  throw error;
			     }
			     
			     if (root.children.length == 0){
			    		hasParents = false;
			    		d3.select("#parentDiv").remove();
			    		d3.selectAll("#childDiv")
			    		  .classed("half", false);
			  	 }
			     
			     var nodes = cluster.nodes(root),
			          links = cluster.links(nodes);
			
			     var link = svgP.selectAll(".link")
			          .data(links)
			          .enter().append("path")
			          .attr("class", "link")
			          .attr("d", d3.svg.diagonal().projection(function(d) { return [orientation_left.x(d), orientation_left.y(d)]; }));
			
			     var node = svgP.selectAll(".node")
			          .data(nodes)
			          .enter().append("g")
			          .attr("class", "node")
			          .attr("transform", function(d) { return "translate(" +  orientation_left.x(d) + "," + orientation_left.y(d) + ")"; })
			
			     node.append("circle")
			          .attr("r", 4.5)
			          .style("fill", function(d) { return d.children ? "lightsteelblue" : "#fff"; })
			          .on('click', function(d, i) {
			        	  window.location.href = "../phenotypes/"  + d.id;
				 });
			    
			     node.append("text")
			          .attr("x", function(d) { return d.children ?  8 : -8; })
			          .attr("y", 3)
			          .attr("class", "treeLabel")
			          .style("text-anchor", function(d) { return d.children ? "start" : "end"; })
			          .text(function(d) { return d.children ? "current page" : shortenLabel(d.name); })
			          .attr("transform", function(d) {return d.children ? "rotate(270)" : ""})
		  		      .style("width", "150px")
		  		      .on('click', function(d, i) {
						  window.location.href = "../phenotypes/"  + d.id;
		  		  }) ; // width of the node labels; 
		    });
	    }
	    
	    if (hasChildren){
		    d3.json("../mpTree/json/" + mp_id + "?type=children", function(error, root) {
		    	  
			    var svg = d3.select("#childDiv").append("svg")
			        .attr("width", width)
			        .attr("height", height)
			        .append("g")
			        .attr("transform", "translate(5,0)");
		    	
			     if (error){ 
			    	  console.log(error);
			    	  throw error;
			   	 }
					     
			     var nodes = cluster.nodes(root),
			          links = cluster.links(nodes);
			
			     var link = svg.selectAll(".link")
			          .data(links)
			          .enter().append("path")
			          .attr("class", "link")
			          .attr("d", d3.svg.diagonal().projection(function(d) { return [orientation_right.x(d), orientation_right.y(d)]; }));
		
			     var node = svg.selectAll(".node")
			          .data(nodes)
			          .enter().append("g")
			          .attr("class", "node")
			          .attr("transform", function(d) { return "translate(" +  orientation_right.x(d) + "," + orientation_right.y(d) + ")"; })
			          
			     node.append("circle")
			          .attr("r", 4.5)
			          .style("fill", function(d) { return d.children ? "lightsteelblue" : "#fff"; })
			          .on('click', function(d, i) {
						  window.location.href = "../phenotypes/"  + d.id;
					   });
			
			     node.append("text")
			          .attr("x", function(d) { return d.children ? -8 : 8; })
			          .attr("y", 3)
			          .attr("class", "treeLabel")
			          .style("text-anchor", function(d) { return d.children ? "start" : "start"; })
			          .attr("transform", function(d) {return d.children ? "rotate(270) translate (15,0)" : ""})
			          .text(function(d) { return d.children ? "current page" : shortenLabel(d.name); })
		   		      .style("width", "150px")
		   		      .on('click', function(d, i) {
						  window.location.href = "../phenotypes/"  + d.id;
					   }); // width of the node labels; 
			     
	
			     if (root.children.length == 0){
			    		hasParents = false;
			    		d3.select("#childDiv").remove();
			    		d3.selectAll("#parentDiv")
			    		  .classed("half", false);
				        svgP.attr("transform", "translate(100,0)");
			  	 }
			     
		    });
	    }
	     
	   
	    d3.select(self.frameElement).style("height", height + "px");
	    