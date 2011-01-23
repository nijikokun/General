package com.nijiko.General;

/**
 * Basic configuration loader.
 * 
 * @author Nijiko
 */
public abstract class DefaultConfiguration {
  public boolean health = true;
  public boolean coords = true;
  public boolean commands = true;
  public String permissionSystem = "default";

  public abstract void load();
}
