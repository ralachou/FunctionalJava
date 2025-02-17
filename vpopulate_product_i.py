import pandas as pd
import ace_tools as tools  # For displaying DataFrames

def populate_product_id(df_1, df_temp1):
    """
    Populates missing productID in df_1 using df_temp1 (mapping of cusip to productID).

    :param df_1: DataFrame with missing productID, but containing cusip
    :param df_temp1: DataFrame with cusip to productID mapping
    :return: Updated df_1 with missing productID filled
    """

    # Merge df_1 with df_temp1 on cusip to get productID
    df_merged = df_1.merge(df_temp1, on="cusip", how="left", suffixes=("_orig", "_mapped"))

    # Fill missing productID in df_1 with values from df_temp1
    df_merged["productID"] = df_merged["productID_orig"].combine_first(df_merged["productID_mapped"])

    # Drop unnecessary columns and return cleaned DataFrame
    df_merged = df_merged.drop(columns=["productID_orig", "productID_mapped"])

    return df_merged

# Example DataFrames
df_1 = pd.DataFrame({
    "cusip": ["1234", "5678", "9101", "1121"],
    "productID": [None, None, "P3", None],  # Missing productID
    "tag": ["filter1", "filter1", "filter2", "filter1"]
})

df_temp1 = pd.DataFrame({
    "cusip": ["1234", "5678", "9101"],  # Mapping table
    "productID": ["P1", "P2", "P3"]
})

# Run function to populate missing productID
df_1_updated = populate_product_id(df_1, df_temp1)

# Display updated df_1
tools.display_dataframe_to_user("Updated df_1 with productID", df_1_updated)
