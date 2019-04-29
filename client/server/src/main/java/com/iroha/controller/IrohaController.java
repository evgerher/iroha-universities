package com.iroha.controller;

import com.iroha.service.IrohaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/iroha")
public class IrohaController {

  private final IrohaService irohaService;

  @Autowired
  public IrohaController(@Qualifier("getIrohaService") IrohaService irohaService) {
    this.irohaService = irohaService;
  }

  @RequestMapping(value = "", method = RequestMethod.POST)
  public String startIrohaNodes() {
    irohaService.startBlockchain();
    return "Iroha network successfully initialized, please wait ~1min";
  }
}
