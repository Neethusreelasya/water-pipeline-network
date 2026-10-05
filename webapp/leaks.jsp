
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.LeakReport, java.util.*" %>
<html>
<head><title>Leaks</title><link rel="stylesheet" href="css/style.css"></head>
<body>
<nav class="navbar"><h2>💧 Water Network</h2>
  <div><a href="index.html">Home</a> <a href="network.jsp">Network</a> <a href="route.jsp">Find Route</a></div>
</nav>

<h2>Report a Leak</h2>
<form action="reportLeak" method="post">
  Area ID: <input name="areaId" required>
  Severity (1-5): <input name="severity" type="number" min="1" max="5" required>
  Description: <input name="description" required>
  <button type="submit">Report</button>
</form>

<h2>Unresolved Leaks (most severe first) <a href="criticalLeaks">[refresh]</a></h2>
<table>
<tr><th>ID</th><th>Area</th><th>Severity</th><th>Description</th></tr>
<%
  List<LeakReport> leaks = (List<LeakReport>) request.getAttribute("leakList");
  if (leaks != null) {
    for (LeakReport l : leaks) {
      String cls = l.getSeverity() >= 4 ? "severity-high" : l.getSeverity() >= 2 ? "severity-med" : "severity-low";
%>
<tr class="<%=cls%>"><td><%=l.getId()%></td><td><%=l.getAreaId()%></td><td><%=l.getSeverity()%></td><td><%=l.getDescription()%></td></tr>
<%  }} %>
</table>
</body>
</html>