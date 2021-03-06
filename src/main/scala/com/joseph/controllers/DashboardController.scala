package com.joseph.controllers

import java.security.Principal

import com.joseph.dao.services.{ItemService, UserService, ViewsService}
import com.joseph.domain.{Item, Status, User}
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import scala.collection.JavaConverters._

/**
  * Controller for all administration actions
 *
  * @param userService
  * @param itemService
  */

@Controller
@RequestMapping(Array("/dashboard"))
class DashboardController @Autowired()(userService: UserService, itemService: ItemService,viewsService: ViewsService) {

  @GetMapping
  @ResponseBody
  def dashboard(): String= {
    val body = new JSONObject()

    val userCount = userService.userCount()

    val itemsCount = itemService.itemsCount()

    val likesCount = itemService.likesCount()

    body.put("users", userCount)
    body.put("items", itemsCount)
    body.put("likes", likesCount)
    body.put("views", viewsService.findCount())
    body.put("today",viewsService.today())
    body.put("weekly",itemService.weeklyPosts())

    //add fake items weekly
    //add fake
    body.toString()
  }

  @GetMapping(Array("/users"))
  @ResponseBody
  def findAllUsers(principal:Principal): java.util.List[User] = {
    userService.findAll().asScala.dropWhile(_.email==principal.getName).asJava
    //return all users except the current one

  }

  @GetMapping(Array("/users/{userId}"))
  @ResponseBody
  def findUser(@PathVariable("userId") userId: String): Any = {
    if (userService.exists(userId)) {
      userService.findUser(userId)
    }
    new Status("error", "user does not exist")
  }

  @GetMapping(Array("/items/{itemId}"))
  @ResponseBody
  def findItem(@PathVariable("itemId") itemId: String): Any = {
    if (itemService.exists(itemId)) {
      itemService.findOne(itemId)
    }
    new Status("error", "item does not exist")
  }

  @GetMapping(Array("/items"))
  @ResponseBody
  def findAllItems(): java.util.List[Item] = {
    itemService.findAll()
  }

  @PostMapping(Array("/users/delete/{userId}"))
  @ResponseBody
  def deleteUser(@PathVariable("userId") userId: String): Status = {
    if (userService.exists(userId)) {
      val user = userService.findUser(userId)
      itemService.deleteUserItems(user)
      userService.deleteUser(user)
      new Status("success", "User deleted")
    } else {
      new Status("error", "User does not exist")
    }
  }

  @PostMapping(Array("/items/delete/{itemId}"))
  @ResponseBody
  def deleteItem(@PathVariable("itemId") itemId: String): Status = {
    if (itemService.exists(itemId)) {
      itemService.removeItem(itemService.findOne(itemId))
      new Status("success", "item deleted")
    } else {
      new Status("error", "Item does not exist")
    }
  }

  @PostMapping(Array("/users/suspend/{userId}"))
  @ResponseBody
  def suspendUser(@PathVariable("userId") userId: String): Status = {
    if (userService.exists(userId)) {
      val user = userService.findUser(userId)
      user.setActive(false)
      userService.save(user)
      //  itemService.suspendByUser(user)
      new Status("success", "user suspended")
    } else {
      new Status("error", "user does not exist")
    }
  }

  @PostMapping(Array("/users/unsuspend/{userId}"))
  @ResponseBody
  def unsuspendUser(@PathVariable("userId") userId: String): Status = {
    if (userService.exists(userId)) {
      val user = userService.findUser(userId)
      user.setActive(true)
      userService.save(user)
      //  itemService.suspendByUser(user)
      new Status("success", "user unsuspended")
    } else {
      new Status("error", "user does not exist")
    }
  }

  @PostMapping(Array("/items/suspend/{itemId}"))
  @ResponseBody
  def suspendItem(@PathVariable("itemId") itemId: String): Status = {
    if (itemService.exists(itemId)) {
      val item = itemService.findOne(itemId)
      item.setIsPublished(false)
      itemService.save(item)
      new Status("success", "Item unpublished")
    } else {
      new Status("error", "item does not exist")
    }
  }

  @PostMapping(Array("/items/unsuspend/{itemId}"))
  @ResponseBody
  def unsuspendItem(@PathVariable("itemId") itemId: String): Status = {
    if (itemService.exists(itemId)) {
      val item = itemService.findOne(itemId)
      item.setIsPublished(true)
      itemService.save(item)
      new Status("success", "Item published")
    } else {
      new Status("error", "item does not exist")
    }
  }

  @GetMapping(Array("/users/between"))
  @ResponseBody
  def usersBetween(@RequestParam("start") start:Long,@RequestParam("end") end:Long):java.util.ArrayList[User]={
    userService.userBetween(start,end)
  }
  @GetMapping(Array("/items/between"))
  @ResponseBody
  def itemsBetween(@RequestParam("start") start:Long,@RequestParam("end") end:Long):java.util.ArrayList[Item]={
    itemService.itemBetween(start,end)
  }
}
