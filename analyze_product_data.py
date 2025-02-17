import pandas as pd
import ace_tools as tools  # For displaying DataFrames

def analyze_product_data(df_1, df_2, df_3, wanted_columns_df2, wanted_columns_df3, output_file="product_analysis.xlsx"):
    """
    Analyzes df_1 against df_2 (pricing data) and df_3 (referential data), 
    identifying missing entries and merging required columns.

    :param df_1: DataFrame loaded from Excel containing productID, cusips, tag
    :param df_2: DataFrame with pricing information (has productID)
    :param df_3: DataFrame with referential information (has productID)
    :param wanted_columns_df2: List of columns to extract from df_2
    :param wanted_columns_df3: List of columns to extract from df_3
    :param output_file: Name of the output Excel file
    :return: Merged DataFrame with analysis
    """

    # Ensure the key column exists in all DataFrames
    for df, name in zip([df_1, df_2, df_3], ["df_1", "df_2", "df_3"]):
        if "productID" not in df.columns:
            raise ValueError(f"'productID' must exist in {name}")

    # Filter df_1 to keep only tag = 'filter1' for df_2 comparison
    df_1_filtered = df_1[df_1["tag"] == "filter1"]

    ### Step 1: Merge df_1 with df_2 (Price Data)
    df_merged = df_1.merge(df_2[["productID"] + wanted_columns_df2], on="productID", how="left")

    # Identify missing productIDs from df_2
    df_merged["missing_price_from_df2"] = df_merged["productID"].isin(df_2["productID"])
    df_merged["missing_price_from_df2"] = df_merged["missing_price_from_df2"].apply(lambda x: "" if x else "Missing Price Data")

    ### Step 2: Merge df_1 with df_3 (Referential Data)
    df_merged = df_merged.merge(df_3[["productID"] + wanted_columns_df3], on="productID", how="left")

    # Identify missing productIDs from df_3
    df_merged["missing_refData"] = df_merged["productID"].isin(df_3["productID"])
    df_merged["missing_refData"] = df_merged["missing_refData"].apply(lambda x: "" if x else "Missing Ref Data")

    ### Step 3: Generate Summary Statistics
    summary_data = {
        "Total Products in df_1": len(df_1),
        "Total Products in df_2": len(df_2),
        "Total Products in df_3": len(df_3),
        "Matching Products in df_2": df_merged["missing_price_from_df2"].eq("").sum(),
        "Missing Products in df_2": df_merged["missing_price_from_df2"].ne("").sum(),
        "Matching Products in df_3": df_merged["missing_refData"].eq("").sum(),
        "Missing Products in df_3": df_merged["missing_refData"].ne("").sum(),
    }

    summary_df = pd.DataFrame(list(summary_data.items()), columns=["Metric", "Value"])

    ### Step 4: Save Data to Excel
    with pd.ExcelWriter(output_file, engine="xlsxwriter") as writer:
        df_merged.to_excel(writer, sheet_name="Final Data", index=False)
        summary_df.to_excel(writer, sheet_name="Summary", index=False)

    # Display results
    tools.display_dataframe_to_user("Final Merged Data", df_merged)
    tools.display_dataframe_to_user("Summary Report", summary_df)

    return df_merged, summary_df

# Example DataFrames
df_1 = pd.DataFrame({
    "productID": ["P1", "P2", "P3", "P4"],
    "cusips": ["1234", "5678", "9101", "1121"],
    "tag": ["filter1", "filter1", "filter2", "filter1"]
})

df_2 = pd.DataFrame({
    "productID": ["P1", "P3"],
    "lastDate": ["2024-01-01", "2024-02-15"],
    "price": [100, 200],
    "namespace": ["N1", "N2"],
    "Error": [None, None]
})

df_3 = pd.DataFrame({
    "productID": ["P1", "P2"],
    "cusips": ["1234", "5678"],
    "maturityDate": ["2030-12-31", "2040-06-30"],
    "productType": ["Bond", "Equity"]
})

# Define wanted columns
wanted_columns_df2 = ["lastDate", "price"]
wanted_columns_df3 = ["maturityDate", "productType"]

# Run the analysis
final_df, summary_df = analyze_product_data(df_1, df_2, df_3, wanted_columns_df2, wanted_columns_df3)

