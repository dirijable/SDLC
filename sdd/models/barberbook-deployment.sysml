package BarberBookDeployment {
  part def Browser;
  part def ApplicationServer;
  part def DatabaseServer;
  part def Deployment {
    part client : Browser;
    part app : ApplicationServer;
    part database : DatabaseServer;
    connect client to app;
    connect app to database;
  }
  part production : Deployment;
}
