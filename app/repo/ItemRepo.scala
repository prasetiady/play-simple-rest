package	com.prasetiady.models

import com.prasetiady.connection.{DBComponent, MySqlDBComponent}
import scala.concurrent.Future
import shop.yaml._

trait ItemRepo extends ItemTable { this: DBComponent =>

  import driver.api._

  implicit def newItemToItem(newItem: NewItem): Item = Item(0, newItem.name, newItem.price, newItem.description)

  /**
   * @param item
   * create new item
   */
  def create(item: NewItem): Future[Int] = db.run { itemsAutoInc += item }

  /**
   * @param item
   * update existing item
   */
  def update(item: Item): Future[Int] = db.run { itemsTableQuery.filter(_.id === item.id).update(item) }

  /**
   * @param id
   * Get item by id
   */
  def getById(id: Int): Future[Option[Item]] = db.run { itemsTableQuery.filter(_.id === id).result.headOption }

  /**
   * @return
   * Get all items
   */
  def getAll(): Future[List[Item]] = db.run { itemsTableQuery.to[List].result }

  /**
   * @param id
   * delete item by id
   */
  def delete(id: Int): Future[Int] = db.run { itemsTableQuery.filter(_.id === id).delete }
}

private[models] trait ItemTable { this: DBComponent =>

  import driver.api._

  private[ItemTable] class ItemTable(tag: Tag) extends Table[Item](tag, "ITEMS") {
    val id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    val name = column[String]("NAME")
    val price = column[Double]("PRICE")
    val description = column[String]("DESCRIPTION")
    def * = (id, name, price, description.?) <> (Item.tupled, Item.unapply)
  }

  protected val itemsTableQuery = TableQuery[ItemTable]

  protected def itemsAutoInc = itemsTableQuery returning itemsTableQuery.map(_.id)
}

object ItemRepo extends ItemRepo with MySqlDBComponent
