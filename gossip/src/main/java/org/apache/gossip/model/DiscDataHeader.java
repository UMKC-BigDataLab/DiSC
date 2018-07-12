package org.apache.gossip.model;

import java.net.URI;
import java.util.HashSet;

import org.apache.gossip.LocalGossipMember;

import com.google.common.hash.BloomFilter;

public class DiscDataHeader {

  public LocalGossipMember mmbr;
  public URI uri;
  public DiscGossipMsgType type;
  public long clockTick;
  public BloomFilter bloomFamilies;

  public DiscDataHeader(LocalGossipMember mmbr, URI uri, DiscGossipMsgType type,
      long clockTick) {
    this.mmbr = mmbr;
    this.uri = uri;
    this.type = type;
    this.clockTick = clockTick;
       
   //this.bloomFilterFamilies = BloomFilter.create(Funnels.stringFunnel(Charset.forName("UTF-8")), 0, 1);
   
  }
}