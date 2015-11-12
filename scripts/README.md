These scripts are used for continuous build and deployment. 

Setting up a jenkins box
======

Spin up a new target machine at [openstack](https://cloud.int.godaddy.com/compute).

Choose Centos6 (java 1.8 rpm exists here).  Also add the DropWizard IP firewall rules. These are opening up
8080 and 8081 on TCP internal. 

Once your machine is running, execute from the root folder of your project

```
./scripts/setup/setup_jenkins.sh
```

Make sure to add your slave's host name `machines` array variable in the [deploy_rpm](deploy_scripts/deploy_rpm) file

### Simplifying ssh access to the slave

If you want to make your life easier, add your public key to the deploy and your local users authorized keys section.

For osx users do

```
# copy your public key to the remote box for passwordless login
ssh-copy-id $testBoxHostName

# copy your public key to the remote box as the deploy user for passwordless login to the deploy user account
ssh-copy-id deploy@$testBoxHostName
```

The deploy users password is `deploy`.