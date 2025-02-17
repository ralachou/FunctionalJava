import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import ace_tools as tools  # For displaying dataframes

def compare_large_dataframes(df1, df2, key_column_df1, key_column_df2):
    """
    Compares two large DataFrames based on a common key (with different column names) and generates a detailed summary.

    :param df1: Smaller DataFrame (e.g., 16,000 rows)
    :param df2: Larger DataFrame (e.g., 600,000 rows)
    :param key_column_df1: Column name in df1 to compare
    :param key_column_df2: Column name in df2 to compare
    :return: Summary statistics and comparison breakdown
    """

    # Ensure the key column exists in both DataFrames
    if key_column_df1 not in df1.columns or key_column_df2 not in df2.columns:
        raise ValueError("Key column must exist in both DataFrames")

    # Convert key columns to string type to avoid mismatches
    df1[key_column_df1] = df1[key_column_df1].astype(str)
    df2[key_column_df2] = df2[key_column_df2].astype(str)

    # Find matching rows
    matching_rows = df1[df1[key_column_df1].isin(df2[key_column_df2])]

    # Find missing rows (exists in df1 but not in df2)
    missing_rows = df1[~df1[key_column_df1].isin(df2[key_column_df2])]

    # Join DataFrames on key columns to compare values in shared columns
    merged_df = df1.merge(df2, left_on=key_column_df1, right_on=key_column_df2, suffixes=('_df1', '_df2'))

    # Find differences in shared columns (excluding key columns)
    diff_columns = [col for col in df1.columns if col != key_column_df1 and col in df2.columns]
    diffs = {}

    for col in diff_columns:
        diffs[col] = (merged_df[f"{col}_df1"] != merged_df[f"{col}_df2"]).sum()

    # Generate Summary
    summary = {
        "Total rows in df1": len(df1),
        "Total rows in df2": len(df2),
        "Matching rows": len(matching_rows),
        "Missing rows": len(missing_rows),
        "Percentage Match": round((len(matching_rows) / len(df1)) * 100, 2),
        "Column Differences": diffs
    }

    # Display DataFrames
    tools.display_dataframe_to_user("Matching Rows", matching_rows)
    tools.display_dataframe_to_user("Missing Rows", missing_rows)

    # Generate Visualizations
    visualize_comparison(summary, matching_rows, missing_rows)

    return summary

def visualize_comparison(summary, matching_rows, missing_rows):
    """
    Generates visualizations for the comparison summary.
    """
    labels = ["Matching Rows", "Missing Rows"]
    values = [summary["Matching rows"], summary["Missing rows"]]

    plt.figure(figsize=(6, 6))
    plt.pie(values, labels=labels, autopct='%1.1f%%', colors=['green', 'red'], startangle=140)
    plt.title("DataFrame Comparison: Match vs Missing")
    plt.show()

    # Show column differences if available
    if "Column Differences" in summary and summary["Column Differences"]:
        plt.figure(figsize=(8, 4))
        sns.barplot(x=list(summary["Column Differences"].keys()), y=list(summary["Column Differences"].values()))
        plt.xticks(rotation=45)
        plt.ylabel("Count of Differences")
        plt.title("Differences in Shared Columns")
        plt.show()

# Example DataFrames (simulating large sets)
data1 = {"securityID": ["A123", "B456", "C789", "D101"], "value": [100, 200, 300, 400]}
data2 = {"productID": ["A123", "C789", "E999", "F111"], "value": [110, 300, 500, 600]}

df1 = pd.DataFrame(data1)  # Smaller set (16,000 rows simulated)
df2 = pd.DataFrame(data2)  # Larger set (600,000 rows simulated)

# Compare DataFrames
summary = compare_large_dataframes(df1, df2, key_column_df1="securityID", key_column_df2="productID")

# Display summary
print(summary)
