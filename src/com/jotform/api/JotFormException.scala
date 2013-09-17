package com.jotform.api


class JotFormException(msg: String) extends RuntimeException(msg) {
  
  def create(msg: String): JotFormException = {
    return new JotFormException(msg)
  }
}