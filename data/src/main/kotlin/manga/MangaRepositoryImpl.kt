/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.core.db.toOptional
import tachiyomi.core.stdlib.Optional
import tachiyomi.data.manga.resolver.MangaDetailsUpdatePutResolver
import tachiyomi.data.manga.resolver.MangaFlagsPutResolver
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.data.manga.util.asDbManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

internal class MangaRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : MangaRepository {

  override fun subscribeManga(mangaId: Long): Flowable<Optional<Manga>> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_ID} = ?")
        .whereArgs(mangaId)
        .build())
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged()
      .map { it.toOptional() }
  }

  override fun subscribeManga(key: String, sourceId: Long): Flowable<Optional<Manga>> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_KEY} = ? AND ${MangaTable.COL_SOURCE} = ?")
        .whereArgs(key, sourceId)
        .build())
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged()
      .map { it.toOptional() }
  }

  override fun getManga(mangaId: Long): Maybe<Manga> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_ID} = ?")
        .whereArgs(mangaId)
        .build())
      .prepare()
      .asRxMaybe()
  }

  override fun getManga(key: String, sourceId: Long): Maybe<Manga> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_KEY} = ? AND ${MangaTable.COL_SOURCE} = ?")
        .whereArgs(key, sourceId)
        .build())
      .prepare()
      .asRxMaybe()
  }

  override fun updateMangaDetails(manga: Manga): Completable {
    return storio.put()
      .`object`(manga)
      .withPutResolver(MangaDetailsUpdatePutResolver())
      .prepare()
      .asRxCompletable()
  }

  override fun saveAndReturnNewManga(manga: MangaInfo, sourceId: Long): Single<Manga> {
    val newManga = manga.asDbManga(sourceId)
    return storio.put()
      .`object`(newManga)
      .prepare()
      .asRxSingle()
      .map { newManga.copy(id = it.insertedId()!!) }
  }

  override fun setFlags(manga: Manga, flags: Int): Completable {
    return storio.put()
      .`object`(manga.copy(flags = flags))
      .withPutResolver(MangaFlagsPutResolver())
      .prepare()
      .asRxCompletable()
  }

  override fun deleteNonFavorite(): Completable {
    return storio.delete()
      .byQuery(DeleteQuery.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_FAVORITE} = ?")
        .whereArgs(0)
        .build())
      .prepare()
      .asRxCompletable()
  }
}
