# **Test SA linked to APP - datafeed**

**Prerequirements:**
- An existing SA (with roles Individual, User Provisioning, Agent Management) and its RSA keys

**Set in Bdk class:**
- the pod URL
- your SA private key
- your SA public key

**Go into CreateUse class:**
- Set a agentToCreate username of your choice
- run it
- a SA and related app with same appId as SA username will be created (using same keys as your user provisioning SA)

**Go into StartDatafeed class:**
- set botUsername as the username you have chosen in previous step
- run it

**Expected results:** no error, it should run forever with datafeed started
