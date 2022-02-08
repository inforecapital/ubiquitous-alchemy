/**
 * Created by Jacob Xie on 1/20/2021.
 */

import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  ManyToOne,
  CreateDateColumn,
  Index
} from "typeorm"

import {record} from "../common"
import {Author} from "./author.entity"
import {Dashboard} from "./dashboard.entity"
import {Template} from "./template.entity"
import {Element} from "./element.entity"

@Entity({name: record})
export class Record {

  @PrimaryGeneratedColumn("uuid")
  id!: string

  @ManyToOne(() => Author, a => a.records, {nullable: false})
  author!: Author

  @ManyToOne(() => Dashboard, d => d.records, {nullable: false})
  dashboard!: Dashboard

  @ManyToOne(() => Template, t => t.records, {nullable: false})
  template!: Template

  @ManyToOne(() => Element, e => e.records, {nullable: false})
  element!: Element

  @Column("text", {nullable: true})
  note?: string

  @CreateDateColumn()
  @Index()
  createdAt!: string
}

