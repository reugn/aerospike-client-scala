package io.github.reugn.aerospike.scala.listener

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait PromiseLike[T] {

  private val p: Promise[T] = Promise[T]()

  /**
   * Completes the underlying promise with a value.
   */
  def success(value: T): this.type = {
    p.complete(Success(value))
    this
  }

  /**
   * Completes the underlying promise with an exception.
   */
  def failure(cause: Throwable): this.type = {
    p.complete(Failure(cause))
    this
  }

  /**
   * Future containing the value of the underlying promise.
   */
  def future: Future[T] = p.future
}
