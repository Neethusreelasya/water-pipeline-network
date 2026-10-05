<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<html>
<head><title>Find Route</title><link rel="stylesheet" href="css/style.css"></head>
<body>
<nav class="navbar"><h2>💧 Water Network</h2>
  <div><a href="index.html">Home</a> <a href="network.jsp">Network</a> <a href="leaks.jsp">Leaks</a></div>
</nav>

<h2>Find Supply Route</h2>
<form action="findRoute" method="get">
  From: <input name="sourceId" required>
  To: <input name="targetId" required>
  <button type="submit">Find Route</button>
</form>

<%
  List<String> path = (List<String>) request.getAttribute("path");
  if (path != null) {
    if (path.isEmpty()) {
%>
      <p class="error">No route found.</p>
<%  } else { %>
      <div class="route-path">
<%      for (int i = 0; i < path.size(); i++) { %>
          <span class="route-box"><%= path.get(i) %></span>
<%        if (i < path.size() - 1) { %> <span class="arrow">→</span> <% } %>
<%      } %>
      </div>
<%  } } %>
</body>
</html>