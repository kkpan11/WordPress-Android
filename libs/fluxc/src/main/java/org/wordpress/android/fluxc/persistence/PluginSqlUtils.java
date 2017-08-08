package org.wordpress.android.fluxc.persistence;

import android.support.annotation.NonNull;

import com.wellsql.generated.PluginInfoModelTable;
import com.wellsql.generated.PluginModelTable;
import com.yarolegovich.wellsql.WellSql;

import org.wordpress.android.fluxc.model.PluginInfoModel;
import org.wordpress.android.fluxc.model.PluginModel;
import org.wordpress.android.fluxc.model.SiteModel;

import java.util.List;

public class PluginSqlUtils {
    public static List<PluginModel> getPlugins(@NonNull SiteModel site) {
        return WellSql.select(PluginModel.class)
                .where()
                .equals(PluginModelTable.LOCAL_SITE_ID, site.getId())
                .endWhere().getAsModel();
    }

    public static void insertOrReplacePlugins(@NonNull SiteModel site, @NonNull List<PluginModel> plugins) {
        // Remove previous plugins for this site
        WellSql.delete(PluginModel.class)
                .where()
                .equals(PluginModelTable.LOCAL_SITE_ID, site.getId())
                .endWhere().execute();
        // Insert new plugins for this site
        for (PluginModel pluginModel : plugins) {
            pluginModel.setLocalSiteId(site.getId());
        }
        WellSql.insert(plugins).execute();
    }

    public static int insertOrUpdatePluginInfo(PluginInfoModel pluginInfo) {
        if (pluginInfo == null) {
            return 0;
        }

        // Slug is the primary key in remote, so we should use that to identify PluginInfoModels
        if (getPluginInfoBySlug(pluginInfo.getSlug()) == null) {
            WellSql.insert(pluginInfo).execute();
            return 1;
        } else {
            return WellSql.update(PluginInfoModel.class)
                    .where().equals(PluginInfoModelTable.SLUG, pluginInfo.getSlug()).endWhere()
                    .put(pluginInfo).execute();
        }
    }

    public static PluginInfoModel getPluginInfoBySlug(String slug) {
        List<PluginInfoModel> result = WellSql.select(PluginInfoModel.class)
                .where().equals(PluginInfoModelTable.SLUG, slug)
                .endWhere().getAsModel();
        return result.isEmpty() ? null : result.get(0);
    }
}
