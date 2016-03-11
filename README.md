# digital-atg
ATG related configurations and source files

In order to build,
Make sure git, java 1.7, ATG 11.1 and ant are installed

1) Clone repository into workspace as required

2) cd into folder RAX

3) make sure DYNAMO_HOME environment is set 

4) ant clean all

This should build the required assets and then create a module RAX within ATG installation folder. The module to include with your BCC ear is RAX.Cloud

REST Support:
Since session confirmation is turn on, it is important that the first actor chain to be called is the following: 

curl -L -v -c customer_cookies.txt -H "Content-Type: application/json" "http://52.2.31.4:20180/rest/model/atg/rest/SessionConfirmationActor/getSessionConfirmationNumber"

Make sure to capture the response within a file "customer_cookies.txt" in the above example as this file would be used later for our form interaction handling.

Since session confirmation is required to prevent cross site attacks, a _dynSessConf  paramater needs to  be appended. The parameter value needs to be obtained via the first actor chain -->/atg/rest/SessionConfirmationActor/getSessionConfirmationNumber"
 

1) To output the topology information: 

curl -L -v -b customer_cooks.txt -H "Content-Type: application/json" "http://52.2.31.4:20180/rest/model/rax/deployment/TopologyActor/info"

2) To create a new agent: 

curl -L -v -b customer_cookies.txt -H "Content-Type: application/json" -d "{"targetName":\"Production\", "agentDisplayName":\"store-test01\", "description":\"store-test01\", "transportURL":\"rmi://test.com:20560/atg/epub/AgentTransport\" }" "http://52.2.31.4:20180/rest/model/rax/deployment/TopologyActor/update?_dynSessConf=12312434324"

3) To initialize once a new agent is added:

curl -L -v -b customer_cookies.txt -H "Content-Type: applicaon/json" -d "{"targetName":\"Production\"}" "http://52.2.31.4:20180/rest/model/rax/deployment/TopologyActor/initialize?_dynSessConf=1231232455"


URL support:

1) To obtain topology as an XML: 
http://52.2.31.4:20180/raxcloud/info.jsp

2) To add a new agent: 
http://52.2.31.4:20180/raxcloud/updateTopology.jsp

